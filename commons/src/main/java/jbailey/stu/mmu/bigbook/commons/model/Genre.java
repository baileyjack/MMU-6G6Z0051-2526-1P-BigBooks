package jbailey.stu.mmu.bigbook.commons.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single Genre object from the DB.
 * Seperated by comma in the DB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    private String name;
}
