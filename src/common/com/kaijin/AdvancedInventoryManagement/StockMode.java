package com.kaijin.AdvancedInventoryManagement;

public enum StockMode
{
	NORMAL(true, true, true),
	REPLACE(true, false, true),
	INSERT(false, false, true),
	REMOVE(false, true, false);

	public final boolean allowReplace; // Invalid items can be removed and replaced if valid items are available
	public final boolean allowRemove; // Invalid items can be removed from a slot
	public final boolean allowInsert; // Valid items can be added to an empty slot

	StockMode(boolean replace, boolean remove, boolean insert)
	{
		allowReplace = replace;
		allowRemove = remove;
		allowInsert = insert;
	}

	public static StockMode getMode(int id)
	{
		if (id < 0 || id >= StockMode.values().length) id = 0; // Bounds checking
		return StockMode.values()[id];
	}

	public static StockMode next(StockMode current)
	{
		switch (current)
		{
		case NORMAL:
			return REPLACE;
		case REPLACE:
			return INSERT;
		case INSERT:
			return REMOVE;
		default:
			return NORMAL;
		}
	}
}
