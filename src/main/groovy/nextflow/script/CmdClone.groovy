/*
 * Copyright (c) 2013-2014, Centre for Genomic Regulation (CRG).
 * Copyright (c) 2013-2014, Paolo Di Tommaso and the respective authors.
 *
 *   This file is part of 'Nextflow'.
 *
 *   Nextflow is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Nextflow is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Nextflow.  If not, see <http://www.gnu.org/licenses/>.
 */

package nextflow.script
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.extension.FilesExtensions
import nextflow.share.PipelineManager

/**
 * CLI sub-command clone
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@Parameters(commandDescription = "Clone a pipeline the specified folder")
class CmdClone implements CmdX {

    @Parameter(required=true, description = 'The name of the pipeline to clone')
    List<String> args

    @Parameter(names='-r', description = 'Revision to clone. Can be a git branch, tag or revision number')
    String revision

    @Override
    void run() {
        // the pipeline name
        String pipeline = args[0]
        final manager = new PipelineManager(pipeline)

        // the target directory is the second parameter
        // otherwise default the current pipeline name
        def target = new File(args.size()> 1 ? args[1] : manager.getBaseName())
        if( target.exists() ) {
            if( target.isFile() )
                throw new IllegalStateException("A file with the same name already exists: $target")
            if( !FilesExtensions.empty(target) )
                throw new IllegalStateException("Clone target directory must be empty: $target")
        }
        else if( !target.mkdirs() ) {
            throw new IOException("Cannot create clone target directory: $target")
        }

        manager.clone(target, revision)
    }
}
