package com.example.helatrack

import com.example.helatrack.data.sms.MessageParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MessageParserTest {

    @Test
    fun `parse M-Pesa Merchant payment with Ksh`() {
        val sender = "MPESA"
        val message = "SJK71234XX Confirmed. Ksh 700.00 paid to Dave Paints."

        val result = MessageParser.parse(sender, message)

        assertNotNull("Result should not be null", result)
        assertEquals(700.0, result?.amount ?: 0.0, 0.0)
        assertEquals("SJK71234XX", result?.ref)
        assertEquals("Dave Paints", result?.person)
    }

    @Test
    fun `parse M-Pesa Merchant payment with KES`() {
        val sender = "MPESA" // Fixed: Was AIRTEL in your previous version
        val message = "SJK71234XX Confirmed. KES 700.00 paid to Dave Paints."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        // Testing that the regex correctly handles "KES" instead of "Ksh"
        assertEquals(700.0, result?.amount ?: 0.0, 0.0)
    }

    @Test
    fun `parse M-Pesa money received`() {
        val sender = "MPESA"
        val message = "SJK71234XX Confirmed. Ksh 1,200.00 received from Jane Doe 254712345678."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(1200.0, result?.amount ?: 0.0, 0.0)
        assertEquals("Jane Doe", result?.person)
    }

    @Test
    fun `parse Airtel Money transaction`() {
        val sender = "AIRTEL"
        val message = "ID: 987654321. Amount: Kes 450.00 from TRM Supermarket on 15/4/26."

        val result = MessageParser.parse(sender, message)

        assertNotNull(result)
        assertEquals(450.0, result?.amount ?: 0.0, 0.0)
        assertEquals("987654321", result?.ref)
    }

//    @Test
//    fun `parse Equity Bank transaction`() {
//        val sender = "Equity Bank"
//        val message = "Ref: EQ12345. Ksh 5,000.00 by ATM Withdrawal."
//
//        val result = MessageParser.parse(sender, message)
//
//        assertNotNull(result)
//        assertEquals(5000.0, result?.amount ?: 0.0, 0.0)
//        assertEquals("ATM Withdrawal", result?.person)
//    }
}