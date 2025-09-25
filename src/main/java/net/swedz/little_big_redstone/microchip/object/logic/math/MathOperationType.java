package net.swedz.little_big_redstone.microchip.object.logic.math;

import net.swedz.little_big_redstone.gui.logicconfig.button.iconcycle.IconCycleLogicConfigButtonIcon;

import java.util.function.Function;

public enum MathOperationType implements IconCycleLogicConfigButtonIcon
{
	ADDITION(7 * 18, 0, (inputs) ->
	{
		int sum = 0;
		for(int input : inputs)
		{
			sum += input;
		}
		return sum;
	}),
	SUBTRACTION(8 * 18, 0, (inputs) ->
	{
		int difference = 0;
		for(int index = 0; index < inputs.length; index++)
		{
			int input = inputs[index];
			if(index == 0)
			{
				difference = input;
			}
			else
			{
				difference -= input;
			}
		}
		return difference;
	});
	
	private final int u, v;
	
	private final Function<int[], Integer> operation;
	
	MathOperationType(int u, int v, Function<int[], Integer> operation)
	{
		this.u = u;
		this.v = v;
		this.operation = operation;
	}
	
	public String textureName()
	{
		return this.name().toLowerCase();
	}
	
	@Override
	public int u()
	{
		return u;
	}
	
	@Override
	public int v()
	{
		return v;
	}
	
	public int execute(int[] inputs)
	{
		return operation.apply(inputs);
	}
}
