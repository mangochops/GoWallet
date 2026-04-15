package com.example.helatrack

import com.example.helatrack.data.sms.MessageParser
import org.junit.Assert.*
import org.junit.Test

class MessageParserTest {

    // --- M-PESA TESTS ---

    @Test
    fun `parse M-Pesa Merchant with KES currency`() {
        val sender = "MPESA"
        val message = "SJK71234XX Confirmed. KES 700.00 paid to Dave Paints."

        val result = MessageParser.parse(sender, message)

        assertNotNull("Should parse KES correctly", result)
        assertEquals(700.0, result?.amount ?: 0.0, 0.0)
        assertEquals("Dave Paints", result?.person)
        assertEquals("MPESA", result?.category)
    }

    @Test
    fun `parse M-Pesa Received with Ksh and comma`() {
        val sender = "MPESA"
        val message = "SJK71234XX Confirmed. Ksh 1,250.00 received from Jane Doe 254712345678."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(1250.0, result?.amount ?: 0.0, 0.0) // Tests cleanAmount comma removal
        assertEquals("Jane Doe", result?.person)
    }

    // --- AIRTEL TESTS ---

    @Test
    fun `parse Airtel money with KES`() {
        val sender = "AIRTEL"
        val message = "ID: 987654321. Amount: KES 450.00 from TRM Supermarket on 15/4/26."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(450.0, result?.amount ?: 0.0, 0.0)
        assertEquals("987654321", result?.ref)
        assertEquals("AIRTEL", result?.category)
    }

    // --- BANKING TESTS ---

    @Test
    fun `parse Family Bank via shortcode`() {
        val sender = "222111" // Matches your Family Bank branch
        val message = "Confirmed. Ksh 3000.00 by Transfer from John Doe."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(3000.0, result?.amount ?: 0.0, 0.0)
        assertTrue(result?.ref?.startsWith("BNK-") == true)
        assertEquals("BANK", result?.category)
    }

    @Test
    fun `parse Equity Bank and verify name truncation`() {
        val sender = "247247" // Matches your Equity branch
        val message = "Confirmed. Ksh 500.00 by Paybill to ThisIsAVeryLongMerchantNameThatShouldBeTruncated."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(500.0, result?.amount ?: 0.0, 0.0)
        // Verifies your .take(25) logic in the Equity branch
        assertTrue("Name should be truncated to 25 chars", (result?.person?.length ?: 0) <= 25)
        assertTrue(result?.ref?.startsWith("EQ-BNK-") == true)
    }

    // --- EDGE CASES ---

    @Test
    fun `return null for irrelevant system messages`() {
        val sender = "MPESA"
        val message = "Your M-PESA balance was KES 5,000.00. Transaction cost was 0.00."

        val result = MessageParser.parse(sender, message)

        // Neither Received nor Merchant regex should match this
        assertNull(result)
    }
}