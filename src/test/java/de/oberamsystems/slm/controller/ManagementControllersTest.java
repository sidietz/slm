package de.oberamsystems.slm.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManagementControllersTest {

    @Test
    public void testManagementControllers() {
        assertEquals("habit-management", new HabitManagementController().index());
        assertEquals("misc-management", new MiscManagementController().index());
        assertEquals("book-management", new BookManagementController().index());
        assertEquals("activity-management", new ActivityManagementController().index());
        assertEquals("spend-management", new SpendManagementController().index());
        assertEquals("human-management", new HumanManagementController().index());
    }
}
