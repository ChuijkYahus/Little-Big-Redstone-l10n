package net.swedz.little_big_redstone.microchip.object.logic.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.swedz.little_big_redstone.LBR;
import net.swedz.little_big_redstone.LBRText;
import net.swedz.little_big_redstone.LBRTooltips;
import net.swedz.little_big_redstone.microchip.object.logic.config.LogicConfig;
import net.swedz.little_big_redstone.microchip.object.logic.config.LogicConfigButtonReference;
import net.swedz.little_big_redstone.microchip.object.logic.config.LogicConfigMenuBuilder;
import net.swedz.tesseract.neoforge.api.range.IntRange;
import net.swedz.tesseract.neoforge.helper.CodecHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static net.swedz.little_big_redstone.LBRTextLine.*;

public final class CalculatorConfig extends LogicConfig<CalculatorConfig>
{
	public static final Codec<CalculatorConfig> CODEC = RecordCodecBuilder.create((instance) -> instance
			.group(
					Codec.INT.optionalFieldOf("input_count", 2).forGetter((config) -> config.inputs),
					CodecHelper.forLowercaseEnum(MathOperationType.class).optionalFieldOf("operation", MathOperationType.ADDITION).forGetter((config) -> config.operation)
			)
			.apply(instance, CalculatorConfig::new));
	
	public static final StreamCodec<ByteBuf, CalculatorConfig> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, (config) -> config.inputs,
			CodecHelper.forEnumStream(MathOperationType.class), (config) -> config.operation,
			CalculatorConfig::new
	);
	
	public int inputs;
	
	public MathOperationType operation;
	
	private CalculatorConfig(int inputs, MathOperationType operation)
	{
		this.inputs = inputs;
		this.operation = operation;
	}
	
	public CalculatorConfig()
	{
		this.inputs = this.inputsAllowed().min();
		this.operation = MathOperationType.ADDITION;
	}
	
	@Override
	public IntRange inputsAllowed()
	{
		return new IntRange(2, 10);
	}
	
	@Override
	public int inputs()
	{
		return inputs;
	}
	
	@Override
	public IntRange outputsAllowed()
	{
		return new IntRange(1, 1);
	}
	
	@Override
	public int outputs()
	{
		return 1;
	}
	
	@Override
	public void appendHoverText(List<Component> lines)
	{
		lines.add(line(LBRText.LOGIC_CONFIG_TOOLTIP_INPUTS).arg(inputs));
		lines.add(line(LBRText.LOGIC_CONFIG_TOOLTIP_MATH_OPERATION).arg(operation, LBRTooltips.MATH_OPERATION_PARSER));
	}
	
	@Override
	public boolean hasMenu()
	{
		return true;
	}
	
	private LBRText operationTooltip()
	{
		return switch (operation)
		{
			case ADDITION -> LBRText.LOGIC_CONFIG_BUTTON_TOOLTIP_CALCULATOR_ADDITION;
			case SUBTRACTION -> LBRText.LOGIC_CONFIG_BUTTON_TOOLTIP_CALCULATOR_SUBTRACTION;
		};
	}
	
	@Override
	public void buildMenu(LogicConfigMenuBuilder builder, int width, int height)
	{
		var operationButton = new AtomicReference<LogicConfigButtonReference<MathOperationType>>();
		
		builder.addSlider(LBRText.LOGIC_CONFIG_BUTTON_LABEL_INPUTS.text(), Component.empty(), LBRText.LOGIC_CONFIG_BUTTON_TOOLTIP_INPUTS.text(), 0, 0, width - 18 - 4, 18, this.inputsAllowed().min(), this.inputsAllowed().max(), inputs, 1, 0, (value) -> inputs = value.intValue());
		
		operationButton.set(builder.addCycleButton(this.operationTooltip().text(), width - 18, 0, LBR.id("textures/gui/slot_atlas.png"), operation, Arrays.asList(MathOperationType.values()), (value) ->
		{
			operation = value;
			var button = operationButton.get();
			if(button != null)
			{
				button.setTooltip(this.operationTooltip().text());
			}
		}));
	}
	
	@Override
	protected void internalLoadFrom(CalculatorConfig other)
	{
		inputs = this.inputsAllowed().clamp(other.inputs);
		operation = other.operation;
	}
	
	@Override
	public void resetForPickup()
	{
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(inputs, operation);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return this == o ||
			   (o instanceof CalculatorConfig other && inputs == other.inputs && operation == other.operation);
	}
}
