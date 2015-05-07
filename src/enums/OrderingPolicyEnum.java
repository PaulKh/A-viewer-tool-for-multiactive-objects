package enums;

/**
 * Created by Paul on 06/05/15.
 */

public enum OrderingPolicyEnum {
    ENABLED, // value = 0
    DISABLED; // value = 1
    public static OrderingPolicyEnum getOrderingPolicyByValue(int value){
        switch (value){
            case 0:
                return ENABLED;
            default: case 1:
                return DISABLED;
        }
    }
    public static int getValueByOrderingPolicy(OrderingPolicyEnum policyEnum){
        switch (policyEnum){
            case DISABLED:
                return 1;
            default: case ENABLED:
                return 0;
        }
    }
    public static int getDefaultValue(){
        return 0;
    }

}
