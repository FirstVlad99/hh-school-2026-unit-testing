package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

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
    libraryManager.addBook("Number1",10);
    libraryManager.addBook("Number2",5);
  }

  @ParameterizedTest
  @CsvSource({
      "Number1, -5",
      "Number2, -7",
      "Number1, -100"
  })
  void testAddNegativeQuantityBooks(
      String bookId,
      int quantity
  ){
      libraryManager.addBook(bookId,quantity);

      int booksCount = libraryManager.getAvailableCopies(bookId);
      assertTrue(booksCount > 0);
  }

  @ParameterizedTest
  @CsvSource({
      "Number1, 5, 15",
      "Number2, 7, 12"
  })
  void testAddAnotherBook(
      String bookId,
      int quantity,
      int expectedTotalQuantity
  ){

    libraryManager.addBook(bookId,quantity);

    int booksCount = libraryManager.getAvailableCopies(bookId);
    assertEquals(booksCount,expectedTotalQuantity);
  }

  @Test
  void testBorrowBookIfUserIsNotActive() {
    Mockito.when(userService.isUserActive(any()))
        .thenReturn(false);

    boolean borrowBookSuccess = libraryManager.borrowBook("Number1", "15");

    assertFalse(borrowBookSuccess);
  }

  @Test
  void testBorrowBookIfThereAreNoCopies(){
    Mockito.when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean borrowBookSuccess = libraryManager.borrowBook("NoCopies", "15");

    assertFalse(borrowBookSuccess);
  }

  @Test
  void testBorrowBook(){
    Mockito.when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean borrowBookSuccess = libraryManager.borrowBook("Number1", "15");

    assertTrue(borrowBookSuccess);
    assertEquals(9, libraryManager.getAvailableCopies("Number1"));
  }

  @Test
  void testBorrowTwoBookCopies(){
    Mockito.when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean firstBorrowBookSuccess = libraryManager.borrowBook("Number1", "15");
    boolean secondBorrowBookSuccess = libraryManager.borrowBook("Number1", "7");

    // использую рефлексию для доступа к borrowedBooks,
    // т.к. при взятии двух книг по одному bookId, количество копий уменьшится,
    // но при этом запись о взятии будет только об одной (из-за дубликата ключа в hashmap)
    Map booksRent;
    Field field;

    try {
      field = libraryManager.getClass().getDeclaredField("borrowedBooks");
      field.setAccessible(true);
      booksRent = (Map) field.get(libraryManager);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    assertTrue(firstBorrowBookSuccess);
    assertTrue(secondBorrowBookSuccess);
    assertEquals(2,booksRent.size());
    assertEquals(8, libraryManager.getAvailableCopies("Number1"));
  }

  @Test
  void testReturnBookIfItDoesNotBorrowed(){
    boolean returnBookSuccess = libraryManager.returnBook("Number1", "15");

    assertFalse(returnBookSuccess);
  }

  @Test
  void testReturnBookIfUserIdIsDifferent(){
    Mockito.when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean firstBorrowBookSuccess = libraryManager.borrowBook("Number1", "15");
    boolean returnBookSuccess = libraryManager.returnBook("Number1", "5");

    assertTrue(firstBorrowBookSuccess);
    assertFalse(returnBookSuccess);
  }

  @Test
  void testReturnBook(){
    Mockito.when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean firstBorrowBookSuccess = libraryManager.borrowBook("Number1", "15");
    boolean returnBookSuccess = libraryManager.returnBook("Number1", "15");

    assertTrue(firstBorrowBookSuccess);
    assertTrue(returnBookSuccess);
    assertEquals(10, libraryManager.getAvailableCopies("Number1"));
  }

  @Test
  void testReturnTwoBookCopies(){
    Mockito.when(userService.isUserActive(any()))
        .thenReturn(true);

    boolean firstBorrowBookSuccess = libraryManager.borrowBook("Number1", "15");
    boolean secondBorrowBookSuccess = libraryManager.borrowBook("Number1", "7");

    boolean firstReturnBookSuccess = libraryManager.returnBook("Number1", "7");
    boolean secondReturnBookSuccess = libraryManager.returnBook("Number1", "15");

    assertTrue(firstBorrowBookSuccess);
    assertTrue(secondBorrowBookSuccess);
    assertTrue(firstReturnBookSuccess);
    assertTrue(secondReturnBookSuccess);
    assertEquals(10, libraryManager.getAvailableCopies("Number1"));
  }

  @ParameterizedTest
  @CsvSource({
      "5, false, false, 2.5",
      "70, true, false, 52.5",
      "700, false, true, 280",
      "7000, true, true, 4200",
  })
  void testCalculateDynamicLateFee(
      int overdueDays,
      boolean isBestseller,
      boolean isPremiumMember,
      double expectedTotalFee
  ){
    double totalFee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
    assertEquals(expectedTotalFee, totalFee);
  }
  @Test
  void testCalculateDynamicLateFeeShouldThrowExceptionIfOverdueDaysAreNegative(){
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-500, false, false)
    );
    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }












}