/*
 * Copyright 2020-2022, Seqera Labs
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

package nextflow.cli

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Global
import nextflow.config.ConfigBuilder
import nextflow.daemon.DaemonLauncher
import nextflow.extension.FilesEx
import nextflow.file.FileHelper
import nextflow.plugin.Plugins
import nextflow.util.ServiceDiscover
import nextflow.util.ServiceName

import java.nio.file.Paths

/**
 * CLI-command NODE
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@Parameters
class CmdS3test extends CmdBase {

    static final public NAME = 's3test'

    @Override
    final String getName() { NAME }

    @Parameter(names ='-source', arity = 1, description='Define source file to copy from s3')
    String sourceFile = ""


    @Override
    void run() {
        def s3Key = System.getenv("S3_KEY")
        def s3Secret = System.getenv("S3_SECRET")

        def config =
                [
                    aws :
                    [
                        region : "Bielefeld",
                        accessKey : s3Key,
                        secretKey : s3Secret,
                        client :
                        [
                            s_3_path_style_access : true,
                            endpoint : "https://openstack.cebitec.uni-bielefeld.de:8080",
                            protocol : "HTTPS",
                            signer_override : 'AWSS3V4SignerType'
                        ]
                    ]
                ]
        Global.setConfig(config)
        Plugins.init()
        Plugins.load([plugins: ["nf-amazon@1.9.0"] ])
        Plugins.start("nf-amazon")
        def source = FileHelper.asPath("s3:///nf-out-large-file/SRR10158848/1/readMapping/0.1.0/interleaved/SRR10158848_interleaved.fq.gz") // Paths.get(new URI("s3:///lratz/nf-s3.test"))
        def target = FileHelper.asPath("./SRR10158848_interleaved.fq.gz")
        println("STARTING DOWNLOAD")
        FilesEx.copyTo(source, target)
        println("DONE")
    }


    /**
     * Launch the daemon service
     *
     * @param config The nextflow configuration map
     */
    protected launchDaemon(String name = null) {

    }

    /**
     * Load a {@code DaemonLauncher} instance of the its *friendly* name i.e. the name provided
     * by using the {@code ServiceName} annotation on the daemon class definition
     *
     * @param name The executor name e.g. {@code gridgain}
     * @return The daemon launcher instance
     * @throws IllegalStateException if the class does not exist or it cannot be instantiated
     */
    static DaemonLauncher loadDaemonByName( String name ) {

    }

    /**
     * Load a class implementing the {@code DaemonLauncher} interface by the specified class name
     *
     * @param name The fully qualified class name e.g. {@code nextflow.executor.LocalExecutor}
     * @return The daemon launcher instance
     * @throws IllegalStateException if the class does not exist or it cannot be instantiated
     */
    static DaemonLauncher loadDaemonByClass( String name ) {

    }

    /**
     * @return The first available instance of a class implementing {@code DaemonLauncher}
     * @throws IllegalStateException when no class implementing {@code DaemonLauncher} is available
     */
    static DaemonLauncher loadDaemonFirst() {
        Plugins.setup()
        final loader = Plugins.getExtension(DaemonLauncher)
        if( !loader )
            throw new IllegalStateException("No cluster services are available -- Cannot launch Nextflow in cluster mode")

        return loader
    }

}
