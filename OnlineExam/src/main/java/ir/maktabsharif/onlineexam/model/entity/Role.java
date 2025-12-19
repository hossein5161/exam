package ir.maktabsharif.onlineexam.model.entity;
import ir.maktabsharif.onlineexam.model.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class Role extends BaseEntity<Long> {

    @Column(unique = true, nullable = false)
    private String name;

 
    public String getPersianName() {
        return RoleType.getPersianName(this.name);
    }
}

