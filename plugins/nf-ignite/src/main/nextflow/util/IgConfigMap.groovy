package nextflow.util

import org.apache.ignite.binary.BinaryObjectException
import org.apache.ignite.binary.BinaryReader
import org.apache.ignite.binary.BinaryWriter
import org.apache.ignite.binary.Binarylizable

class IgConfigMap extends LinkedHashMap implements Serializable, Binarylizable {

    IgConfigMap(Map content) {
        super(content)
    }

    @Override
    void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        this.writeObject(this)
    }

    @Override
    void readBinary(BinaryReader reader) throws BinaryObjectException {

    }
}
