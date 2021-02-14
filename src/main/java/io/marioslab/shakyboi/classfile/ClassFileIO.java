package io.marioslab.shakyboi.classfile;

import io.marioslab.shakyboi.util.ByteArrayInput;
import io.marioslab.shakyboi.util.ByteArrayOutput;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class ClassFileIO {

    public static byte[] writeClassFile(ClassFile clz) throws IOException {
        ByteArrayOutput out = new ByteArrayOutput(8192);

        out.writeInt(clz.magic);
        out.writeShort(clz.minorVersion);
        out.writeShort(clz.majorVersion);
        out.writeShort(clz.constantPool.size());

        // Skip first entry, skip null entries for long/double
        for (int i = 1; i < clz.constantPool.size(); i++)
            writeConstantPoolEntry(out, clz.constantPool.get(i));

        out.writeShort(clz.accessFlags);
        out.writeShort(clz.thisClass);
        out.writeShort(clz.superClass);

        out.writeShort(clz.interfaces.size());
        for (int i = 0; i < clz.interfaces.size(); i++)
            out.writeShort(clz.interfaces.get(i));

        out.writeShort(clz.fields.size());
        for (int i = 0; i < clz.fields.size(); i++)
            writeMember(out, clz.fields.get(i));

        out.writeShort(clz.methods.size());
        for (int i = 0; i < clz.methods.size(); i++) {
            writeMember(out, clz.methods.get(i));
        }

        out.writeShort(clz.attributes.size());
        for (int i = 0; i < clz.attributes.size(); i++)
            writeAttribute(out, clz.attributes.get(i));

        return out.toByteArray();
    }

    public static void writeMember(ByteArrayOutput out, ClassFile.MemberInfo member) throws IOException {
        out.writeShort(member.accessFlags);
        out.writeShort(member.nameIndex);
        out.writeShort(member.descriptorIndex);

        writeAttributes(out, member.attributes);
    }

    public static void writeAttributes(ByteArrayOutput out, List<ClassFile.AttributeInfo> attributes) throws IOException {
        out.writeShort(attributes.size());
        for (ClassFile.AttributeInfo attribute : attributes)
            writeAttribute(out, attribute);

    }

    public static void writeAttribute(ByteArrayOutput out, ClassFile.AttributeInfo attribute) throws IOException {
        out.writeShort(attribute.nameIndex);
        out.writeInt(attribute.info.length);
        out.write(attribute.info);
    }

    public static void writeConstantPoolEntry(ByteArrayOutput out, ClassFile.ConstantPoolEntry entry) throws IOException {
        if (entry == null) return;

        int tag = entry.tag;
        out.write(tag);

        if (entry instanceof ClassFile.Utf8PoolEntry) {
            ClassFile.Utf8PoolEntry utf8 = (ClassFile.Utf8PoolEntry) entry;
            out.writeShort(utf8.bytes.length);
            out.write(utf8.bytes);
        } else if (entry instanceof ClassFile.IntPoolEntry) {
            out.writeInt(((ClassFile.IntPoolEntry) entry).value);
        } else if (entry instanceof ClassFile.FloatPoolEntry) {
            out.writeFloat(((ClassFile.FloatPoolEntry) entry).value);
        } else if (entry instanceof ClassFile.LongPoolEntry) {
            ClassFile.LongPoolEntry longEntry = (ClassFile.LongPoolEntry) entry;
            out.writeInt(longEntry.highBytes);
            out.writeInt(longEntry.lowBytes);
        } else if (entry instanceof ClassFile.DoublePoolEntry) {
            ClassFile.DoublePoolEntry doubleEntry = (ClassFile.DoublePoolEntry) entry;
            out.writeInt(doubleEntry.highBytes);
            out.writeInt(doubleEntry.lowBytes);
        } else if (entry instanceof ClassFile.PackagePoolEntry) {
            out.writeShort(((ClassFile.PackagePoolEntry) entry).nameIndex);
        } else if (entry instanceof ClassFile.ModulePoolEntry) {
            out.writeShort(((ClassFile.ModulePoolEntry) entry).nameIndex);
        } else if (entry instanceof ClassFile.ClassPoolEntry) {
            out.writeShort(((ClassFile.ClassPoolEntry) entry).nameIndex);
        } else if (entry instanceof ClassFile.StringPoolEntry) {
            out.writeShort(((ClassFile.StringPoolEntry) entry).stringIndex);
        } else if (entry instanceof ClassFile.FieldRefPoolEntry) {
            ClassFile.FieldRefPoolEntry fmiEntry = (ClassFile.FieldRefPoolEntry) entry;
            out.writeShort(fmiEntry.classIndex);
            out.writeShort(fmiEntry.nameAndTypeIndex);
        } else if (entry instanceof ClassFile.MethodRefPoolEntry) {
            ClassFile.MethodRefPoolEntry fmiEntry = (ClassFile.MethodRefPoolEntry) entry;
            out.writeShort(fmiEntry.classIndex);
            out.writeShort(fmiEntry.nameAndTypeIndex);
        } else if (entry instanceof ClassFile.InterfaceMethodRefPoolEntry) {
            ClassFile.InterfaceMethodRefPoolEntry fmiEntry = (ClassFile.InterfaceMethodRefPoolEntry) entry;
            out.writeShort(fmiEntry.classIndex);
            out.writeShort(fmiEntry.nameAndTypeIndex);
        } else if (entry instanceof ClassFile.NameAndTypePoolEntry) {
            ClassFile.NameAndTypePoolEntry ntEntry = (ClassFile.NameAndTypePoolEntry) entry;
            out.writeShort(ntEntry.nameIndex);
            out.writeShort(ntEntry.descriptorIndex);
        } else if (entry instanceof ClassFile.MethodHandlePoolEntry) {
            ClassFile.MethodHandlePoolEntry mEntry = (ClassFile.MethodHandlePoolEntry) entry;
            out.write(mEntry.referenceKind);
            out.writeShort(mEntry.referenceIndex);
        } else if (entry instanceof ClassFile.MethodTypePoolEntry) {
            out.writeShort(((ClassFile.MethodTypePoolEntry) entry).descriptorIndex);
        } else if (entry instanceof ClassFile.DynamicPoolEntry) {
            ClassFile.DynamicPoolEntry dEntry = (ClassFile.DynamicPoolEntry) entry;
            out.writeShort(dEntry.bootstrapMethodAttributeIndex);
            out.writeShort(dEntry.nameAndTypeIndex);
        } else if (entry instanceof ClassFile.InvokeDynamicPoolEntry) {
            ClassFile.InvokeDynamicPoolEntry dEntry = (ClassFile.InvokeDynamicPoolEntry) entry;
            out.writeShort(dEntry.bootstrapMethodAttributeIndex);
            out.writeShort(dEntry.nameAndTypeIndex);
        } else {
            throw new RuntimeException("Unknown constant pool entry: " + entry.getClass().getName());
        }
    }

    public static ClassFile readClassFile(String name, byte[] data) {
        DataInputStream in = new DataInputStream(new ByteArrayInput(data));
        try {
            ClassFile clz = new ClassFile(name, data);

            if (in.readInt() != 0xcafebabe) throw new RuntimeException("Magic 0xcafebabe not found");

            clz.minorVersion = in.readUnsignedShort();
            clz.majorVersion = in.readUnsignedShort();

            readConstantPool(in, clz);

            clz.accessFlags = in.readUnsignedShort();
            clz.thisClass = in.readUnsignedShort();
            clz.superClass = in.readUnsignedShort();

            readInterfaces(in, clz);
            readMembers(in, clz, clz.fields);
            readMembers(in, clz, clz.methods);
            readAttributes(in, clz, clz.attributes);

            return clz;
        } catch (Throwable t) {
            throw new RuntimeException("Error reading " + name, t);
        }
    }

    public static void readConstantPool(DataInputStream in, ClassFile clz) throws IOException {
        int constantPoolCount = in.readUnsignedShort();
        clz.constantPool.add(null);
        for (int i = 1; i <= constantPoolCount - 1; i++) {
            int tag = in.readUnsignedByte();
            ClassFile.ConstantPoolEntry entry = null;

            switch (tag) {
                case ClassFile.CONSTANT_UTF8:
                    int len = in.readUnsignedShort();
                    byte[] bytes = new byte[len];
                    for (int j = 0; j < len; j++)
                        bytes[j] = in.readByte();
                    entry = new ClassFile.Utf8PoolEntry(clz, tag, bytes);
                    break;

                case ClassFile.CONSTANT_INTEGER:
                    entry = new ClassFile.IntPoolEntry(clz, tag, in.readInt());
                    break;

                case ClassFile.CONSTANT_FLOAT:
                    entry = new ClassFile.FloatPoolEntry(clz, tag, in.readFloat());
                    break;

                case ClassFile.CONSTANT_LONG:
                    entry = new ClassFile.LongPoolEntry(clz, tag, in.readInt(), in.readInt());
                    break;

                case ClassFile.CONSTANT_DOUBLE:
                    entry = new ClassFile.DoublePoolEntry(clz, tag, in.readInt(), in.readInt());
                    break;

                case ClassFile.CONSTANT_PACKAGE:
                    entry = new ClassFile.PackagePoolEntry(clz, tag, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_MODULE:
                    entry = new ClassFile.ModulePoolEntry(clz, tag, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_CLASS:
                    entry = new ClassFile.ClassPoolEntry(clz, tag, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_STRING:
                    entry = new ClassFile.StringPoolEntry(clz, tag, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_FIELDREF:
                    entry = new ClassFile.FieldRefPoolEntry(clz, tag, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_METHODREF:
                    entry = new ClassFile.MethodRefPoolEntry(clz, tag, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_INTERFACEMETHODREF:
                    entry = new ClassFile.InterfaceMethodRefPoolEntry(clz, tag, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_NAMEANDTYPE:
                    entry = new ClassFile.NameAndTypePoolEntry(clz, tag, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_METHODHANDLE:
                    entry = new ClassFile.MethodHandlePoolEntry(clz, tag, in.readUnsignedByte(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_METHODTYPE:
                    entry = new ClassFile.MethodTypePoolEntry(clz, tag, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_DYNAMIC:
                    entry = new ClassFile.DynamicPoolEntry(clz, tag, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_INVOKEDYNAMIC:
                    entry = new ClassFile.InvokeDynamicPoolEntry(clz, tag, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                default:
                    throw new RuntimeException("Unknown constant pool entry tag: " + tag);
            }
            clz.constantPool.add(entry);
            if (entry.tag == ClassFile.CONSTANT_LONG || entry.tag == ClassFile.CONSTANT_DOUBLE) {
                clz.constantPool.add(null);
                i++;
            }
        }
    }

    public static void readInterfaces(DataInputStream in, ClassFile clz) throws IOException {
        int interfacesCount = in.readUnsignedShort();
        for (int i = 0; i < interfacesCount; i++)
            clz.interfaces.add(in.readUnsignedShort());
    }

    public static ClassFile.MemberInfo readMember(DataInputStream in, ClassFile clz) throws IOException {
        int accessFlag = in.readUnsignedShort();
        int nameIndex = in.readUnsignedShort();
        int descriptorIndex = in.readUnsignedShort();
        ClassFile.MemberInfo memberInfo = new ClassFile.MemberInfo(clz, accessFlag, nameIndex, descriptorIndex);
        readAttributes(in, clz, memberInfo.attributes);
        return memberInfo;
    }

    public static void readMembers(DataInputStream in, ClassFile clz, List<ClassFile.MemberInfo> members) throws IOException {
        int fieldsCount = in.readUnsignedShort();
        for (int i = 0; i < fieldsCount; i++)
            members.add(readMember(in, clz));
    }

    public static void readAttributes(DataInputStream in, ClassFile clz, List<ClassFile.AttributeInfo> infos) throws IOException {
        int attributesCount = in.readUnsignedShort();
        for (int i = 0; i < attributesCount; i++) {
            infos.add(readAttribute(in, clz));
        }
    }

    public static ClassFile.AttributeInfo readAttribute(DataInputStream in, ClassFile clz) throws IOException {
        int attributeNameIndex = in.readUnsignedShort();
        int attributeLength = in.readInt();
        byte[] info = new byte[attributeLength];
        in.readFully(info);
        return new ClassFile.AttributeInfo(clz, attributeNameIndex, info);
    }
}
