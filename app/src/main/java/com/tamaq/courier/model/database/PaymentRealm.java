package com.tamaq.courier.model.database;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.R;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PaymentRealm extends RealmObject {

    public static final String ID_ROW = "mId";
    public static final String ORDER_ID_ROW = "mOrderId";
    @SerializedName("id")
    @Expose
    @PrimaryKey
    private String mId;
    @SerializedName("date")
    @Expose
    private String mDate;
    @SerializedName("time")
    @Expose
    private String mTime;
    @SerializedName("money")
    @Expose
    private Double mMoney;
    @SerializedName("orderId")
    @Expose
    private String mOrderId;
    @SerializedName("type")
    @Expose
    private String mType;
    private String mDetails;
    private int mOrderNumber;

    public PaymentRealm() {
    }

    public PaymentRealm(PaymentRealm paymentRealm) {
        mId = paymentRealm.getId();
        mDate = paymentRealm.getDate();
        mTime = paymentRealm.getTime();
        mMoney = paymentRealm.getMoney();
        mOrderId = paymentRealm.getOrderId();
        mType = paymentRealm.getType();
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public Double getMoney() {
        return mMoney;
    }

    public void setMoney(Double money) {
        mMoney = money;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public void setOrderId(String orderId) {
        mOrderId = orderId;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getFullDate() {
        return String.format("%s %s", getDate(), getTime());
    }

    public String getDetails() {
        return mDetails;
    }

    public void setDetails(String details) {
        mDetails = details;
    }

    public String getTypeUI(Context context) {
        return Paytype.getTypeByValue(getType(), context);
    }

    public Paytype getTypeEnum() {
        return Paytype.getValueByString(getType());
    }

    public int getOrderNumber() {
        return mOrderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        mOrderNumber = orderNumber;
    }

    public enum Paytype {
        EARN("earn"),
        FILL("fill"),
        HANDFILL("handfill"),
        COMMISSION("commission"),
        MONEYBACK("moneyback"),
        BONUS("bonus"),
        PAYBONUS("paybonus"),
        DUTY("duty");

        String mValue;

        Paytype(String value) {
            mValue = value;
        }

        public static Paytype getValueByString(String string) {
            for (Paytype value : values()) {
                if (value.getValue().equals(string)) return value;
            }
            return null;
        }

        public static String getTypeByValue(String value, Context context) {
            switch (value) {
                case "fill":
                    return context.getString(R.string.fill);
                case "handfill":
                    return context.getString(R.string.handfill);
                case "commission":
                    return context.getString(R.string.commission);
                case "moneyback":
                    return context.getString(R.string.moneyback);
                case "bonus":
                    return context.getString(R.string.bonus);
                case "paybonus":
                    return context.getString(R.string.paybonus);
                case "duty":
                    return context.getString(R.string.duty);
                case "earn":
                    return context.getString(R.string.earn);
                default:
                    return context.getString(R.string.earn);
            }
        }

        public String getValue() {
            return mValue;
        }
    }
}
