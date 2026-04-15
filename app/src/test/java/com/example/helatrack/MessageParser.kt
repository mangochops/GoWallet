package com.example.helatrack

import com.example.helatrack.data.sms.MessageParser
import org.junit.Assert.*
import org.junit.Test

class MessageParserTest {

    // --- M-PESA TESTS ---

    @Test
    fun `parse M-Pesa Merchant and strip trailing period`() {
        val sender = "MPESA"
        // Testing that the period after Paints is NOT included in the name
        val message = "SJK71234XX Confirmed. KES 700.00 paid to Dave Paints."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(700.0, result?.amount ?: 0.0, 0.0)
        assertEquals("Dave Paints", result?.person)
    }

    @Test
    fun `parse M-Pesa Received and strip trailing on`() {
        val sender = "MPESA"
        // Testing that the "on" and date are NOT included in the name
        val message = "SJK71234XX Confirmed. KES 1300.00 received from Villa Rosa on 15/4/26."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(1300.0, result?.amount ?: 0.0, 0.0)
        assertEquals("Villa Rosa", result?.person)
    }

    @Test
    fun `parse M-Pesa Received with Ksh and phone number`() {
        val sender = "MPESA"
        val message = "SJK71234XX Confirmed. Ksh 1,250.00 received from Jane Doe 254712345678."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(1250.0, result?.amount ?: 0.0, 0.0)
        assertEquals("Jane Doe", result?.person)
    }

    // --- AIRTEL TESTS ---

    @Test
    fun `parse Airtel money and strip trailing on`() {
        val sender = "AIRTEL"
        val message = "ID: 987654321. Amount: KES 450.00 from TRM Supermarket on 15/4/26."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(450.0, result?.amount ?: 0.0, 0.0)
        assertEquals("TRM Supermarket", result?.person) // Ensures "on 15/4/26" is gone
        assertEquals("987654321", result?.ref)
    }

    // --- BANKING TESTS ---

    @Test
    fun `parse Family Bank via shortcode and strip trailing on`() {
        val sender = "222111"
        val message = "Confirmed. Ksh 3000.00 by Transfer from John Doe on 15/4/26."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(3000.0, result?.amount ?: 0.0, 0.0)
        assertEquals("Transfer from John Doe", result?.person)
        assertTrue(result?.ref?.startsWith("BNK-") == true)
    }

    @Test
    fun `parse Equity Bank and verify name truncation`() {
        val sender = "247247"
        val message = "Confirmed. Ksh 500.00 by Paybill to ThisIsAVeryLongMerchantNameThatShouldBeTruncated."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(500.0, result?.amount ?: 0.0, 0.0)
        // Verifies the .take(25) logic
        assertTrue("Name should be <= 25 chars", (result?.person?.length ?: 0) <= 25)
        assertTrue(result?.ref?.startsWith("EQ-BNK-") == true)
    }

    // --- NEGATIVE TESTS ---

    @Test
    fun `return null for irrelevant system messages`() {
        val sender = "MPESA"
        val message = "Your M-PESA balance was KES 5,000.00. Transaction cost was 0.00."
        val result = MessageParser.parse(sender, message)
        assertNull(result)
    }
}