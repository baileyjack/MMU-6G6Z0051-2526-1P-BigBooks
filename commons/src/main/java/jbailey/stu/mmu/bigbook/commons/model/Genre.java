package jbailey.stu.mmu.bigbook.commons.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single Genre object from the DB.
 * Separated by comma in the DB.
 * 
 * Seperated by comma in the DB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "Genre")
public class Genre {
    private String name;
}
