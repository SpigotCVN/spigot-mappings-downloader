package io.github.spigotcvn.smdownloader.mappings;

import java.io.File;

public class MappingFile {
    private MappingFileType fileType;
    private MappingType type;
    private File file;

    public MappingFile(MappingType type, File file) {
        this.fileType = MappingFileType.getByName(file.getName());
        this.type = type;
        this.file = file;
    }

    public MappingFile(MappingFileType fileType, MappingType type, File file) {
        this.fileType = fileType;
        this.type = type;
        this.file = file;
    }

    public MappingFileType getFileType() {
        return fileType;
    }

    public MappingType getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    public enum MappingFileType {
        TINY("tiny"),

        SRG("srg"),
        CSRG("csrg"),
        PROGUARD("txt"); // mojang proguard mappigns are shippe in txt

        private final String fileExtension;

        MappingFileType(String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public static MappingFileType getByName(String fileName) {
            for(MappingFileType type : values()) {
                if(type.getFileExtension() == null) continue;
                if(fileName.endsWith("." + type.getFileExtension())) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum MappingType {
        CLASS,
        MEMBERS,
        FIELDS,
        PACKAGE,
        COMBINED;
    }
}
