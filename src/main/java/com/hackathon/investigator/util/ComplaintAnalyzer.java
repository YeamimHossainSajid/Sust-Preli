package com.hackathon.investigator.util;

import java.util.Locale;

public final class ComplaintAnalyzer {

    private ComplaintAnalyzer() {
    }

    public static boolean isVagueComplaint(String complaint) {
        String text = TextUtils.normalize(complaint);
        if (text.isBlank()) {
            return true;
        }

        boolean hasSpecificIssue = TextUtils.containsAny(
                text,
                "wrong number", "wrong account", "wrong person", "wrong transfer",
                "failed", "refund", "duplicate", "twice", "double", "otp", "pin",
                "settlement", "settled", "cash in", "cash-in", "scam", "phishing", "brother",
                "sister", "deducted", "recharge", "bill", "electricity",
                "এজেন্ট", "ক্যাশ", "otp", "pin", "not received", "didn't receive"
        );
        boolean hasAmount = TextUtils.extractAmount(complaint) != null;

        if (hasSpecificIssue || hasAmount) {
            return false;
        }

        return TextUtils.containsAny(
                text,
                "something is wrong", "please check", "help me", "problem with my money",
                "wrong with my money"
        );
    }

    public static boolean mentionsPhishing(String complaint) {
        String text = TextUtils.normalize(complaint);
        return TextUtils.containsAny(
                text,
                "otp", "pin", "password", "phishing", "scam", "called me", "asked for",
                "social engineering", "blocked if", "claim to be", "from bkash"
        );
    }

    public static boolean mentionsDuplicatePayment(String complaint) {
        return TextUtils.containsAny(
                TextUtils.normalize(complaint),
                "twice", "double", "duplicate", "two times", "deducted twice", "paid once"
        );
    }

    public static boolean mentionsWrongTransfer(String complaint) {
        return TextUtils.containsAny(
                TextUtils.normalize(complaint),
                "wrong number", "wrong account", "wrong person", "typed it wrong",
                "wrong recipient", "ভুল নম্বর", "vul number", "galat"
        );
    }

    public static boolean mentionsNotReceived(String complaint) {
        return TextUtils.containsAny(
                TextUtils.normalize(complaint),
                "not received", "didn't receive", "did not receive", "hasn't received",
                "has not received", "didnt get", "didn't get"
        );
    }

    public static boolean mentionsPaymentFailed(String complaint) {
        String text = TextUtils.normalize(complaint);
        return TextUtils.containsAny(text, "failed", "fail", "unsuccessful", "showed failed", "ব্যর্থ")
                && TextUtils.containsAny(text, "payment", "pay", "recharge", "deducted", "balance");
    }

    public static boolean mentionsRefundRequest(String complaint) {
        String text = TextUtils.normalize(complaint);
        return TextUtils.containsAny(text, "refund", "money back", "return my money", "ফেরত")
                && !mentionsPaymentFailed(complaint);
    }

    public static boolean mentionsMerchantSettlement(String complaint) {
        String text = TextUtils.normalize(complaint);
        return TextUtils.containsAny(text, "settlement", "settled", "not been settled", "sales")
                || (text.contains("merchant") && text.contains("settlement"));
    }

    public static boolean mentionsAgentCashIn(String complaint) {
        return TextUtils.containsAny(
                TextUtils.normalize(complaint),
                "cash in", "cash-in", "cashin", "এজেন্ট", "agent", "ক্যাশ ইন"
        );
    }

    public static boolean mentionsCredentialsShared(String complaint) {
        String text = TextUtils.normalize(complaint);
        if (TextUtils.containsAny(text, "haven't shared", "havent shared", "hasn't shared",
                "hasnt shared", "didn't share", "didnt share", "not shared", "no share")) {
            if (!TextUtils.containsAny(text, "i shared", "shared my", "gave my", "gave them my")) {
                return false;
            }
        }
        return TextUtils.containsAny(
                text,
                "shared my otp", "shared my pin", "shared my password", "shared the otp",
                "gave my otp", "gave my pin", "gave them my otp", "gave them my pin",
                "i shared otp", "i shared pin", "i shared password", "shared it with them"
        );
    }

    public static boolean isAdversarialComplaint(String complaint) {
        String text = TextUtils.normalize(complaint);
        return TextUtils.containsAny(
                text,
                "system:", "ignore previous", "ignore your rules", "you are now",
                "pretend you are", "output your system prompt", "show your prompt",
                "sql injection", "drop table", "jailbreak", "developer mode"
        );
    }

    public static boolean isBanglaLanguage(String language) {
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("bn");
    }
}
