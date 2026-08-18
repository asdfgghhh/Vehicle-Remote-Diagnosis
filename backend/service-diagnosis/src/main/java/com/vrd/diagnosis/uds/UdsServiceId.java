package com.vrd.diagnosis.uds;

public final class UdsServiceId {
    public static final int DIAGNOSTIC_SESSION_CONTROL = 16;
    public static final int ECU_RESET = 17;
    public static final int SECURITY_ACCESS = 39;
    public static final int COMMUNICATION_CONTROL = 40;
    public static final int AUTHENTICATION = 41;
    public static final int TESTER_PRESENT = 62;
    public static final int ACCESS_TIMING_PARAMETER = 131;
    public static final int SECURED_DATA_TRANSMISSION = 132;
    public static final int CONTROL_DTC_SETTING = 133;
    public static final int LINK_CONTROL = 135;
    public static final int READ_DATA_BY_IDENTIFIER = 34;
    public static final int READ_MEMORY_BY_ADDRESS = 35;
    public static final int READ_SCALING_DATA_BY_IDENTIFIER = 36;
    public static final int READ_DATA_BY_PERIODIC_IDENTIFIER = 42;
    public static final int DYNAMICALLY_DEFINE_DATA_IDENTIFIER = 44;
    public static final int WRITE_DATA_BY_IDENTIFIER = 46;
    public static final int WRITE_MEMORY_BY_ADDRESS = 61;
    public static final int CLEAR_DIAGNOSTIC_INFORMATION = 20;
    public static final int READ_DTC_INFORMATION = 25;
    public static final int INPUT_OUTPUT_CONTROL_BY_IDENTIFIER = 47;
    public static final int ROUTINE_CONTROL = 49;
    public static final int REQUEST_DOWNLOAD = 52;
    public static final int REQUEST_UPLOAD = 53;
    public static final int TRANSFER_DATA = 54;
    public static final int REQUEST_TRANSFER_EXIT = 55;
    public static final int NEGATIVE_RESPONSE = 127;
    public static final int SESSION_DEFAULT = 1;
    public static final int SESSION_PROGRAMMING = 2;
    public static final int SESSION_EXTENDED = 3;
    public static final int SESSION_SAFETY_SYSTEM = 4;
    public static final int RESET_HARD = 1;
    public static final int RESET_KEY_OFF_ON = 2;
    public static final int RESET_SOFT = 3;
    public static final int NRC_GENERAL_REJECT = 16;
    public static final int NRC_SERVICE_NOT_SUPPORTED = 17;
    public static final int NRC_SUBFUNCTION_NOT_SUPPORTED = 18;
    public static final int NRC_INCORRECT_MESSAGE_LENGTH = 19;
    public static final int NRC_CONDITIONS_NOT_CORRECT = 34;
    public static final int NRC_REQUEST_SEQUENCE_ERROR = 36;
    public static final int NRC_REQUEST_OUT_OF_RANGE = 49;
    public static final int NRC_SECURITY_ACCESS_DENIED = 51;
    public static final int NRC_INVALID_KEY = 53;
    public static final int NRC_EXCEEDED_NUMBER_OF_ATTEMPTS = 54;
    public static final int NRC_REQUIRED_TIME_DELAY_NOT_EXPIRED = 55;
    public static final int NRC_UPLOAD_DOWNLOAD_NOT_ACCEPTED = 112;
    public static final int NRC_TRANSFER_DATA_SUSPENDED = 113;
    public static final int NRC_GENERAL_PROGRAMMING_FAILURE = 114;

    private UdsServiceId() {
    }
}

