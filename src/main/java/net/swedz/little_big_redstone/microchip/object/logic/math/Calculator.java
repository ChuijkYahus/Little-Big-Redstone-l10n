package net.swedz.little_big_redstone.microchip.object.logic.math;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.swedz.little_big_redstone.microchip.object.logic.LogicComponent;
import net.swedz.little_big_redstone.microchip.object.logic.LogicContext;
import net.swedz.little_big_redstone.microchip.object.logic.LogicGridSize;
import net.swedz.little_big_redstone.microchip.object.logic.LogicType;
import net.swedz.little_big_redstone.microchip.object.logic.LogicTypes;

import java.util.Optional;

public final class Calculator extends LogicComponent<Calculator, CalculatorConfig>
{
	public static final MapCodec<Calculator> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance
			.group(
					CalculatorConfig.CODEC.fieldOf("config").forGetter(LogicComponent::config),
					DyeColor.CODEC.optionalFieldOf("color").forGetter(LogicComponent::color),
					Codec.INT.optionalFieldOf("output", 0).forGetter(Calculator::output)
			)
			.apply(instance, Calculator::new));
	
	public static final StreamCodec<ByteBuf, Calculator> STREAM_CODEC = StreamCodec.composite(
			CalculatorConfig.STREAM_CODEC, LogicComponent::config,
			ByteBufCodecs.optional(DyeColor.STREAM_CODEC), Calculator::color,
			ByteBufCodecs.VAR_INT, Calculator::output,
			Calculator::new
	);
	
	private int outputState;
	
	private Calculator(CalculatorConfig config, Optional<DyeColor> color, int outputState)
	{
		super(config, color);
		this.outputState = outputState;
	}
	
	private Calculator(Optional<DyeColor> color, int outputState)
	{
		super(color);
		this.outputState = outputState;
	}
	
	public Calculator()
	{
		this(Optional.empty(), 0);
	}
	
	@Override
	protected CalculatorConfig defaultConfig()
	{
		return new CalculatorConfig();
	}
	
	@Override
	public LogicType<Calculator> type()
	{
		return LogicTypes.CALCULATOR;
	}
	
	@Override
	protected void processTickInternal(LogicContext context, int[] inputs)
	{
		int originalOutputState = outputState;
		outputState = Mth.clamp(config.operation.execute(inputs), 0, 15);
		if(outputState != originalOutputState)
		{
			context.markDirty(this);
		}
	}
	
	@Override
	protected int outputInternal(int index)
	{
		return outputState;
	}
	
	public int output()
	{
		return this.output(0);
	}
	
	@Override
	public LogicGridSize size()
	{
		int inputs = this.inputs();
		return new LogicGridSize(1, Math.max(1, inputs / 2));
	}
	
	@Override
	protected void internalLoadFrom(Calculator other)
	{
		outputState = other.outputInternal(0);
	}
	
	@Override
	public void internalResetForPickup()
	{
		outputState = 0;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.type(), config, color);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return this == o ||
			   (o instanceof Calculator other && Objects.equal(config, other.config) && Objects.equal(color, other.color));
	}
}
