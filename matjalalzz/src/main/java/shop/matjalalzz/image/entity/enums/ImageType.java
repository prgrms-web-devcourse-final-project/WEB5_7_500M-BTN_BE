package shop.matjalalzz.image.entity.enums;

public enum ImageType {
    SHOP_IMG("shops"),
    PROFILE_IMG("profiles"),
    INQUIRY_IMG("inquiry");

    private final String folder;

    ImageType(String folder) { this.folder = folder; }

    public String folder() { return folder; }
}
