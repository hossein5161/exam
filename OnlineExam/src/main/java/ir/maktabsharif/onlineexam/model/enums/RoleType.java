package ir.maktabsharif.onlineexam.model.enums;
import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_ADMIN("ادمین"),
    ROLE_TEACHER("استاد"),
    ROLE_STUDENT("دانشجو");

    private final String persianName;

    RoleType(String persianName) {
        this.persianName = persianName;
    }

    public static RoleType fromString(String roleName) {
        if (roleName == null) {
            return null;
        }
        try {
            return valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String getPersianName(String roleName) {
        RoleType roleType = fromString(roleName);
        return roleType != null ? roleType.getPersianName() : roleName;
    }
}
