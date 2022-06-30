/*
 * Copyright 2013-2019, Centre for Genomic Regulation (CRG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.executor
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import nextflow.file.FileHelper
import nextflow.processor.TaskBean
/**
 * Implements file staging strategy for a Ignite task.
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class IgFileStagingStrategy implements StagingStrategy {

    /**
     * Task meta-data
     */
    protected TaskBean task

    /**
     * Session unique-id
     */
    protected UUID sessionId

    /**
     * The local scratch dir where the task is actually executed in the remote node.
     * Note: is declared transient because it is valid only on the remote-side,
     * thus it do not need to be transported
     *
     */
    protected Path localWorkDir

    /**
     * A temporary where all files are cached. The folder is deleted during instance shut-down
     */
    private Path _localCacheDir

    Path getLocalCacheDir() { _localCacheDir }

    IgFileStagingStrategy() {
        Runtime.getRuntime().addShutdownHook (
                new Thread(() -> _localCacheDir?.deleteDir())
        )
    }


    /**
     * Copies to the task input files to the execution folder, that is {@link #localWorkDir}
     * folder created when this method is invoked
     */
    @Override
    void stage() {

        def useScratchDir = isValidScratchDir(task)

        // create a local scratch dir, use configured scratch if available
        localWorkDir = useScratchDir
                        ? FileHelper.createTempFolder(Paths.get(task.scratch.toString()))
                        : FileHelper.createLocalDir()
        log?.debug "Task ${task.name} >using ${localWorkDir?.toString()} as scratch/local work dir"
        // create local cache dir, use configured scratch if available
        _localCacheDir = useScratchDir
                        ? FileHelper.createTempFolder(Paths.get(task.scratch.toString()))
                        : FileHelper.createLocalDir()

        log?.debug "Task ${task.name} > using ${_localCacheDir?.toString()} as cache dir"

        if( !task.inputFiles )
            return

        // move the input files there
        for( Map.Entry<String,Path> entry : task.inputFiles.entrySet() ) {
            final fileName = entry.key
            final source = entry.value
            final cached = FileHelper.getLocalCachePath(source, localCacheDir, sessionId)
            final staged = localWorkDir.resolve(fileName)
            // create any sub-directory before create the symlink
            if( fileName.contains('/') ) {
                Files.createDirectories(staged.parent)
            }
            log?.debug "Task ${task.name} > staging path: '${source}' to: '$staged'"
            Files.createSymbolicLink(staged, cached)
        }

    }

    /**
     * Copy back the task output files from the execution directory in the local node storage
     * to the task {@link nextflow.processor.TaskRun#getTargetDir()}
     */
    @Override
    void unstage() {

        log?.debug "Task ${task.name} > Unstaging file names: $task.outputFiles"

        if( !task.outputFiles )
            return

        // create a bash script that will copy the out file to the working directory
        if( !Files.exists(task.targetDir) )
            Files.createDirectories(task.targetDir)

        for( String name : task.outputFiles ) {
            try {
                copyToTargetDir(name, localWorkDir, task.targetDir)
            }
            catch( IOException e ) {
                log?.error("Unable to copy result file: $name to target dir", e)
            }
        }
    }


    /**
     * Copy the file with the specified name from the task execution folder
     * to the {@code targetDir}
     *
     * @param filePattern A file name relative to the {@link #localWorkDir}.
     *        It can contain globs wildcards
     */
    @PackageScope
    void copyToTargetDir( String filePattern, Path from, Path to ) {

        def type = filePattern.contains('**') ? 'file' : 'any'

        FileHelper.visitFiles( from, filePattern, type: type ) { Path it ->
            // note: converts to a *string* other the two paths may belong to two different systems and thus throwing an exception
            final rel = from.relativize(it).toString()
            it.copyTo(to.resolve(rel))
        }
    }

    private static boolean isValidScratchDir(TaskBean task) {
        try {
            Paths.get(task?.scratch?.toString())
            return true
        } catch (InvalidPathException ipe) {
            log?.error("Given scratch path '${task?.scratch?.toString()}' is not valid", ipe)
            return false
        }
        catch (Exception e) {
            log?.error("Unexpected error checking scratch path '${task?.scratch?.toString()}'") // , e)
            return false
        }
    }
}
