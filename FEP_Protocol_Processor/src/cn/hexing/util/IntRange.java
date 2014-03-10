package cn.hexing.util;

/**
 * 整数范围
 */
public class IntRange {

    /** 最小值（包含） */
    private int min;
    /** 最大值（包含） */
    private int max;
    private int hashCode = 0;
    
    /**
     * 构造一个整数范围
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     */
    public IntRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must equal or greater than min");
        }
        
        this.min = min;
        this.max = max;
        this.hashCode = 37 * (37 * 17 + min) + max;
    }
    
    /**
     * 判断给定的值是否包含在范围中
     * @param val 给定的值
     * @return true - 包含，false - 不包含
     */
    public boolean contains(int val) {
        return (val >= min && val <= max);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof IntRange)) {
            return false;
        }
        
        IntRange range = (IntRange) obj;
        return (this.min == range.min && this.max == range.max);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return hashCode;
    }
}
