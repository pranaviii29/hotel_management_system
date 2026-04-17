package com.hotel.util;

import com.hotel.model.Booking;
import com.hotel.model.ServiceType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Character Stream usage for receipt generation
public class ReceiptGenerator {

    public static String generateReceipt(Booking booking) {
        StringBuilder sb   = new StringBuilder();
        String line        = "=".repeat(56);
        String thinLine    = "-".repeat(56);

        sb.append(line).append("\n");
        sb.append("          SILVERVIEW GRAND HOTEL\n");
        sb.append("       123 Marine Drive, Udupi, Karnataka\n");
        sb.append("         Tel: +91-824-2200000\n");
        sb.append(line).append("\n");
        sb.append("                BOOKING RECEIPT\n");
        sb.append(thinLine).append("\n");
        sb.append(String.format("Receipt No  : %s\n", booking.getBookingId()));
        sb.append(String.format("Date        : %s\n",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
        sb.append(thinLine).append("\n");

        // Guest
        sb.append("GUEST DETAILS\n");
        sb.append(String.format("  Name      : %s\n", booking.getGuest().getName()));
        sb.append(String.format("  Phone     : %s\n", booking.getGuest().getPhone()));
        if (!booking.getGuest().getEmail().isBlank())
            sb.append(String.format("  Email     : %s\n", booking.getGuest().getEmail()));
        sb.append(String.format("  ID Proof  : %s — %s\n",
            booking.getGuest().getIdProofType(), booking.getGuest().getIdProofNumber()));
        sb.append(thinLine).append("\n");

        // Room
        sb.append("ROOM DETAILS\n");
        sb.append(String.format("  Room No   : %s\n",  booking.getRoom().getRoomNumber()));
        sb.append(String.format("  Type      : %s\n",  booking.getRoom().getRoomType().getDisplayName()));
        sb.append(String.format("  Floor     : %s\n",  booking.getRoom().getFloor()));
        sb.append(String.format("  Check-In  : %s\n",  booking.getCheckInDate()));
        sb.append(String.format("  Check-Out : %s\n",  booking.getCheckOutDate()));
        sb.append(String.format("  Nights    : %d\n",  booking.getNights()));
        sb.append(thinLine).append("\n");

        // Billing
        sb.append("BILLING DETAILS\n");
        sb.append(String.format("  Room Rate       : Rs %,.2f / night\n",
            booking.getRoom().getPricePerNight()));
        sb.append(String.format("  Room Charge     : Rs %,.2f  (%d nights)\n",
            booking.getRoomChargeOnly(), booking.getNights()));

        if (!booking.getServices().isEmpty()) {
            sb.append("  Services        :\n");
            for (ServiceType svc : booking.getServices()) {
                sb.append(String.format("    %-20s Rs %,.2f\n",
                    svc.getDisplayName(), svc.getCharge()));
            }
            sb.append(String.format("  Service Total   : Rs %,.2f\n",
                booking.getServiceAmount()));
        }

        sb.append(String.format("  GST (18%%)       : Rs %,.2f\n", booking.getGstAmount()));
        sb.append(String.format("  Gross Total     : Rs %,.2f\n", booking.getTotalAmount()));

        // ── Refund section (shown only when there are refunds) ─────────────────
        if (booking.getRefundAmount() > 0 && !booking.getRefundedServices().isEmpty()) {
            sb.append(thinLine).append("\n");
            sb.append("REFUND — INCOMPLETE SERVICES\n");
            sb.append("  The following services were requested but not\n");
            sb.append("  completed before checkout. Charges are refunded.\n\n");

            for (ServiceType svc : booking.getRefundedServices()) {
                double svcCharge    = svc.getCharge();
                double gstOnSvc     = svcCharge * 0.18;
                double totalRefund  = svcCharge + gstOnSvc;
                sb.append(String.format("  %-20s Rs %,.2f  (+GST Rs %,.2f)\n",
                    svc.getDisplayName(), svcCharge, gstOnSvc));
                sb.append(String.format("    Refund           : Rs %,.2f\n", totalRefund));
            }

            sb.append(String.format("\n  Total Refunded  : Rs %,.2f\n",
                booking.getRefundAmount()));
            sb.append(thinLine).append("\n");
        }

        // Final amount line
        sb.append(line).append("\n");
        if (booking.getRefundAmount() > 0) {
            sb.append(String.format("AMOUNT CHARGED  : Rs %,.2f\n",
                booking.getFinalAmountAfterRefund()));
            sb.append(String.format("AMOUNT REFUNDED : Rs %,.2f\n",
                booking.getRefundAmount()));
        } else {
            sb.append(String.format("TOTAL AMOUNT    : Rs %,.2f\n",
                booking.getFinalAmountAfterRefund()));
        }
        sb.append(line).append("\n");

        sb.append(String.format("Status      : %s\n", booking.getStatus().getDisplayName()));

        if (booking.getSpecialRequests() != null && !booking.getSpecialRequests().isBlank()) {
            sb.append(thinLine).append("\n");
            sb.append("Special Requests: ").append(booking.getSpecialRequests()).append("\n");
        }

        sb.append(line).append("\n");
        sb.append("     Thank you for choosing Silverview Grand!\n");
        sb.append("         We look forward to your next visit.\n");
        sb.append(line).append("\n");

        saveReceiptToFile(booking.getBookingId(), sb.toString());
        return sb.toString();
    }

    private static void saveReceiptToFile(String bookingId, String receipt) {
        String dir  = FileHandler.getDataDir() + "receipts/";
        new File(dir).mkdirs();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(dir + bookingId + ".txt"))) {
            w.write(receipt);
        } catch (IOException e) {
            System.err.println("Could not save receipt: " + e.getMessage());
        }
    }
}
