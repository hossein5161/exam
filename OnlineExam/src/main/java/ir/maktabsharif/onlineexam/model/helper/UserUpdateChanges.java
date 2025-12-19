package ir.maktabsharif.onlineexam.model.helper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateChanges {
    @Builder.Default
    private Map<String, ChangeDetail> changes = new HashMap<>();
    
    @Builder.Default
    private boolean passwordChanged = false;
    
    public void addChange(String fieldName, String oldValue, String newValue) {
        if (changes == null) {
            changes = new HashMap<>();
        }
        if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
            changes.put(fieldName, ChangeDetail.builder()
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build());
        } else if (oldValue == null && newValue != null) {
            changes.put(fieldName, ChangeDetail.builder()
                    .oldValue("")
                    .newValue(newValue)
                    .build());
        } else if (oldValue != null && newValue == null) {
            changes.put(fieldName, ChangeDetail.builder()
                    .oldValue(oldValue)
                    .newValue("")
                    .build());
        }
    }
    
    public boolean hasChanges() {
        return changes != null && !changes.isEmpty();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeDetail {
        private String oldValue;
        private String newValue;
    }
}
