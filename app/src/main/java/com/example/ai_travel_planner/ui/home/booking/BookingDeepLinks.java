package com.example.ai_travel_planner.ui.home.booking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Tạo deeplink sang các trang đặt vé phổ biến với tham số đã điền sẵn.
 * - Skyscanner: https://www.skyscanner.net/transport/flights/{from}/{to}/{ddMMyy}/{ddMMyy}/?adultsv2=...&cabinclass=...
 * - Kiwi:       https://www.kiwi.com/en/search/results/{from}-{to}/{yyyy-MM-dd}/{yyyy-MM-dd}?adults=...
 * - Google:     https://www.google.com/travel/flights?q=from ... to ... on ...
 */
public final class BookingDeepLinks {

    private BookingDeepLinks() {}

    // ========= Public builders =========

    /** Skyscanner deeplink theo đúng format bạn gửi ví dụ. */
    public static String skyscanner(String fromIata, String toIata,
                                    String departISO, String returnISO,
                                    int adults, String cabin, String currency) {

        String from = safeCode(fromIata);
        String to   = safeCode(toIata);

        String outDDMMYY = isoToDdMMyy(departISO);            // bắt buộc
        String retDDMMYY = (notEmpty(returnISO) ? isoToDdMMyy(returnISO) : null);

        String base = "https://www.skyscanner.net/transport/flights";

        // path: /{from}/{to}/{out}/{ret}/  hoặc /{from}/{to}/{out}/  (one-way)
        StringBuilder path = new StringBuilder()
                .append('/').append(from.toLowerCase(Locale.US))
                .append('/').append(to.toLowerCase(Locale.US))
                .append('/').append(outDDMMYY);

        boolean roundTrip = notEmpty(retDDMMYY);
        if (roundTrip) path.append('/').append(retDDMMYY);

        // query
        // adultsv2: số người lớn; childrenv2 rỗng; rtn=1 (khứ hồi) hoặc 0 (một chiều)
        String c = mapCabinSkyscanner(cabin); // economy|premiumeconomy|business|first

        String query = String.format(Locale.US,
                "?adultsv2=%d&cabinclass=%s&childrenv2=&rtn=%d" +
                        "&preferdirects=false&outboundaltsenabled=false&inboundaltsenabled=false",
                Math.max(1, adults), c, roundTrip ? 1 : 0);

        // currency (tuỳ chọn)
        if (notEmpty(currency)) {
            query += "&currency=" + currency.toLowerCase(Locale.US);
        }

        return base + path + "/" + query;
    }

    /** Kiwi deeplink (ổn định, cũng autofill tốt). */
    public static String kiwi(String fromIata, String toIata,
                              String departISO, String returnISO,
                              int adults, String cabin, String currency) {

        String from = safeCode(fromIata).toUpperCase(Locale.US);
        String to   = safeCode(toIata).toUpperCase(Locale.US);

        String base = "https://www.kiwi.com/en/search/results";
        StringBuilder url = new StringBuilder(base)
                .append('/').append(from).append('-').append(to)
                .append('/').append(safeIso(departISO));

        if (notEmpty(returnISO)) url.append('/').append(safeIso(returnISO));

        url.append("?adults=").append(Math.max(1, adults));
        url.append("&cabinClass=").append(mapCabinKiwi(cabin));      // ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST

        if (notEmpty(currency)) url.append("&currency=").append(currency.toUpperCase(Locale.US));
        return url.toString();
    }

    /** Google Flights (autofill cơ bản, dễ nhất). */
    public static String googleFlights(String fromIata, String toIata,
                                       String departISO, String returnISO,
                                       int adults, String cabin, String currency) {

        String q = "Flights from " + safeCode(fromIata) + " to " + safeCode(toIata)
                + " on " + safeIso(departISO);
        if (notEmpty(returnISO)) q += " returning " + safeIso(returnISO);

        if (adults > 1) q += " " + adults + " adults";
        if (notEmpty(cabin)) q += " " + cabin;

        q = q.replace(" ", "%20");
        return "https://www.google.com/travel/flights?q=" + q;
    }

    // ========= Helpers =========

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String safeCode(String code) {
        return notEmpty(code) ? code.trim() : "";
    }

    private static String safeIso(String iso) {
        return notEmpty(iso) ? iso.trim() : "";
    }

    /** Convert yyyy-MM-dd -> ddMMyy (Skyscanner path). */
    private static String isoToDdMMyy(String iso) {
        if (!notEmpty(iso)) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("ddMMyy", Locale.US);
            return out.format(in.parse(iso));
        } catch (ParseException e) {
            // fallback: nếu parse lỗi, loại gạch & nén lại
            // ví dụ "2025-11-21" -> "211125"
            String digits = iso.replaceAll("[^0-9]", "");
            if (digits.length() >= 6) {
                // yyyyMMdd -> ddMMyy
                if (digits.length() == 8) {
                    String yyyy = digits.substring(0, 4);
                    String mm   = digits.substring(4, 6);
                    String dd   = digits.substring(6, 8);
                    String yy   = yyyy.substring(2);
                    return dd + mm + yy;
                }
                // nếu lỡ format khác, cứ trả về 6 ký tự cuối
                return digits.substring(digits.length() - 6);
            }
            return "010125"; // tránh crash
        }
    }

    private static String mapCabinSkyscanner(String cabin) {
        if (!notEmpty(cabin)) return "economy";
        switch (cabin.toUpperCase(Locale.US)) {
            case "FIRST":            return "first";
            case "BUSINESS":         return "business";
            case "PREMIUM_ECONOMY":  return "premiumeconomy";
            default:                 return "economy";
        }
    }

    private static String mapCabinKiwi(String cabin) {
        if (!notEmpty(cabin)) return "ECONOMY";
        switch (cabin.toUpperCase(Locale.US)) {
            case "FIRST":            return "FIRST";
            case "BUSINESS":         return "BUSINESS";
            case "PREMIUM_ECONOMY":  return "PREMIUM_ECONOMY";
            default:                 return "ECONOMY";
        }
    }
}