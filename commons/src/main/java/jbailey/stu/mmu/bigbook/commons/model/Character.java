package jbailey.stu.mmu.bigbook.commons.model;

// External Imports
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single Character object from the DB.
 * Seperated by comma in the DB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Character {
    private String name;
}
