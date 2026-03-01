package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

  @Mock
  private NotificationService notificationService;
  @Mock
  private UserService userService;
  @InjectMocks
  private LibraryManager libraryManager;

  @BeforeEach
  void setUp() {
    libraryManager.addBook("Number1", 10);
    libraryManager.addBook("Number2", 5);
  }

  @Test
  void testAddAnotherBook() {
    libraryManager.addBook("Number1", 5);

    int booksCount = libraryManager.getAvailableCopies("Number1");
    assertEquals(15, booksCount);
  }

  @Test
  void testBorrowBookIfUserIsNotActive() {
    when(userService.isUserActive(any()))
        .thenReturn(false);

    boolean borrowBookSuccess = libraryManager.borrowBook("Number1", "15");
    verify(notificationService).notifyUser(
        "15",
        "Your account is not active."
    );
    assertFalse(borrowBookSuccess);
  }

  @Test
  void testBorrowBookIfThereAreNoCopies() {
    when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean borrowBookSuccess = libraryManager.borrowBook("NoCopies", "15");
    assertFalse(borrowBookSuccess);
  }

  @Test
  void testBorrowBook() {
    when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean borrowBookSuccess = libraryManager.borrowBook("Number1", "15");
    verify(notificationService).notifyUser(
        "15",
        "You have borrowed the book: Number1"
    );
    assertTrue(borrowBookSuccess);
    assertEquals(9, libraryManager.getAvailableCopies("Number1"));
  }

  @Test
  void testReturnBookIfItDoesNotBorrowed() {
    boolean returnBookSuccess = libraryManager.returnBook("Number1", "15");
    assertFalse(returnBookSuccess);
  }

  @Test
  void testReturnBookIfUserIdIsDifferent() {
    when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean borrowBookSuccess = libraryManager.borrowBook("Number1", "15");
    boolean returnBookSuccess = libraryManager.returnBook("Number1", "5");
    assertTrue(borrowBookSuccess);
    assertFalse(returnBookSuccess);
  }

  @Test
  void testReturnBook() {
    when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean borrowBookSuccess = libraryManager.borrowBook("Number1", "15");
    boolean returnBookSuccess = libraryManager.returnBook("Number1", "15");

    verify(notificationService).notifyUser(
        "15",
        "You have returned the book: Number1"
    );

    assertTrue(borrowBookSuccess);
    assertTrue(returnBookSuccess);
    assertEquals(10, libraryManager.getAvailableCopies("Number1"));
  }

  @ParameterizedTest
  @CsvSource({
      "5, false, false, 2.5",
      "70, true, false, 52.5",
      "700, false, true, 280",
      "7000, true, true, 4200",
      "0, false, false, 0",
  })
  void testCalculateDynamicLateFee(
      int overdueDays,
      boolean isBestseller,
      boolean isPremiumMember,
      double expectedTotalFee
  ) {
    double totalFee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
    assertEquals(expectedTotalFee, totalFee);
  }

  @Test
  void testCalculateDynamicLateFeeShouldThrowExceptionIfOverdueDaysAreNegative() {
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-500, false, false)
    );
    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }
}
