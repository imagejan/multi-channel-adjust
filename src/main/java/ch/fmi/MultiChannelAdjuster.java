package ch.fmi;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.util.ArrayList;
import java.util.List;

import org.scijava.Initializable;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.log.LogService;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.NumberWidget;

/**
 * Dynamically adjust the display range of multi-channel composite images
 * 
 * This plugin displays a pair of sliders per channel to allow per-channel
 * adjustment of the display range, all in a single dialog
 * 
 * @author Jan Eglinger
 */
@Plugin(type = Command.class, menuPath = "Plugins>Examples>Adjust Composite Image Display")
public class MultiChannelAdjuster extends DynamicCommand implements
		Initializable {

	@Parameter
	private ImagePlus imp;

	@Parameter
	private LogService log;

	private List<MutableModuleItem<Integer>> minItems = new ArrayList<>();
	private List<MutableModuleItem<Integer>> maxItems = new ArrayList<>();

	private boolean initialized = false;

	@Override
	public void initialize() {
		if (!initialized) {
			for (int c = 1; c <= imp.getNChannels(); c++) {

				// get (min,max) range for channel
				ImageProcessor currentIp = ((CompositeImage) imp).getProcessor(c);
				ImageStatistics stats = currentIp.getStatistics();
				double channelMin = stats.min;
				double channelMax = stats.max;

				// add Min slider
				final MutableModuleItem<Integer> itemMin = new DefaultMutableModuleItem<>(
						getInfo(), "channelMin" + c, Integer.class);
				itemMin.setLabel("Channel #" + c + " Minimum");
				itemMin.setPersisted(false);
				itemMin.setMinimumValue((int) channelMin);
				itemMin.setMaximumValue((int) channelMax);
				itemMin.setWidgetStyle(NumberWidget.SLIDER_STYLE);
				itemMin.setCallback("adjustmentCallback");
				itemMin.setValue(this, (int) currentIp.getMin());
				minItems.add(itemMin);
				getInfo().addInput(itemMin);

				// add Max slider
				final MutableModuleItem<Integer> itemMax = new DefaultMutableModuleItem<>(
						getInfo(), "channelMax" + c, Integer.class);
				itemMax.setLabel("Channel #" + c + " Maximum");
				itemMax.setPersisted(false);
				itemMax.setMinimumValue((int) channelMin);
				itemMax.setMaximumValue((int) channelMax);
				itemMax.setWidgetStyle(NumberWidget.SLIDER_STYLE);
				itemMax.setCallback("adjustmentCallback");
				itemMax.setValue(this, (int) currentIp.getMax());
				maxItems.add(itemMax);
				getInfo().addInput(itemMax);
			}
			initialized = true;
		}
	}

	@Override
	public void run() {
		// do nothing (keep adjusted display range)
	}

	@SuppressWarnings("unused")
	private void adjustmentCallback() {
		int currentChannel = imp.getC();
		for (int c = 0; c < imp.getNChannels(); c++) {
			// for some reason, updating the current channel
			// doesn't work correctly with getProcessor(channel),
			// so we have to handle the current channel explicitly
			if (c == currentChannel - 1) {
				imp.getProcessor().setMinAndMax( //
						minItems.get(c).getValue(this), //
						maxItems.get(c).getValue(this));
			} else {
				((CompositeImage) imp).getProcessor(c + 1).setMinAndMax( //
						minItems.get(c).getValue(this), //
						maxItems.get(c).getValue(this));

			}
		}
		imp.updateAndDraw();
	}

}
