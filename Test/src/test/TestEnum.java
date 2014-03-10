package test;

public class TestEnum {
	public enum EnumType{type1,typ2};
	public static void main(String[] args) {
		EnumType e = EnumType.typ2;
		System.out.println(e.ordinal());
	}
}
