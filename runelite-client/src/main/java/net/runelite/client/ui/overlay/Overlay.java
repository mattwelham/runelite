/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.ui.overlay;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

@Getter
@Setter
@Slf4j
public abstract class Overlay implements LayoutableRenderableEntity
{
	@Nullable
	private final Plugin plugin;
	private Point preferredLocation;
	private boolean preferredLocationUpdated = false;
	private boolean preferredLocationUsingUIScaling = true;
	private boolean preferredLocationUsingGameScaling = false;
	private float preferredLocationWidthPercent = -1;
	private float preferredLocationHeightPercent = -1;
	private int preferredLocationLeftMargin = -1;
	private int preferredLocationRightMargin = -1;
	private int preferredLocationTopMargin = -1;
	private int preferredLocationBottomMargin = -1;
	private Dimension preferredSize;
	private OverlayPosition preferredPosition;
	private Rectangle bounds = new Rectangle();
	private OverlayPosition position = OverlayPosition.TOP_LEFT;
	private OverlayPriority priority = OverlayPriority.NONE;
	private OverlayLayer layer = OverlayLayer.UNDER_WIDGETS;
	private final List<Integer> drawHooks = new ArrayList<>();
	private final List<OverlayMenuEntry> menuEntries = new ArrayList<>();
	private boolean resizable;
	private int minimumSize = 32;
	private boolean resettable = true;

	/**
	 * Whether this overlay can be dragged onto other overlays & have
	 * other overlays dragged onto it.
	 */
	@Setter(AccessLevel.PROTECTED)
	private boolean dragTargetable;

	protected Overlay()
	{
		plugin = null;
	}

	protected Overlay(Plugin plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Overlay name, used for saving the overlay, needs to be unique
	 *
	 * @return overlay name
	 */
	public String getName()
	{
		return this.getClass().getSimpleName();
	}

	protected void drawAfterInterface(int interfaceId)
	{
		drawHooks.add(interfaceId << 16 | 0xffff);
	}

	protected void drawAfterLayer(WidgetInfo layer)
	{
		drawHooks.add(layer.getId());
	}

	public void onMouseOver()
	{
	}

	/**
	 * Called when an overlay is dragged onto this, if dragTargetable is true.
	 * Return true to consume the mouse event and prevent the other
	 * overlay from being moved
	 *
	 * @param other the overlay being dragged
	 * @return
	 */
	public boolean onDrag(Overlay other)
	{
		return false;
	}

	/**
	 * Get the parent bounds for overlay dragging. The overlay will
	 * not be allowed to be moved outside of the parent bounds.
	 * @return
	 */
	@Nullable
	public Rectangle getParentBounds()
	{
		return null;
	}

	public void setPreferredLocation(final Point preferredLocation)
	{
		if ( preferredLocation != null)
		{
			log.debug("preferredLocation.x: {}", preferredLocation.x);
			log.debug("preferredLocation.y: {}", preferredLocation.y);
			this.preferredLocation = preferredLocation;
			this.preferredLocationUpdated = true;
		}
		else
		{
			log.warn("setPreferredLocation: Location null");
		}
	}

	public void updatePreferredLocationScalingValues(final Dimension clientDimensions)
	{

		//Overlay scales based on game scaling so it stays positioned relative to the game window
		if ( this.preferredLocationUsingGameScaling )
		{
			this.preferredLocationWidthPercent = (float) preferredLocation.x / (float) clientDimensions.width;
			this.preferredLocationHeightPercent = (float) preferredLocation.y / (float) clientDimensions.height;
		}

		//Overlay scales based on UI scaling so it stays positioned relative to UI elements
		if ( this.preferredLocationUsingUIScaling )
		{
			log.debug("preferredLocation.x: {}", preferredLocation.x);
			log.debug("clientDimensions.width: {}", clientDimensions.width);
			//If overlay is on left side of the screen attach it to the left edge of the screen for horizontal positioning
			if (preferredLocation.x <= clientDimensions.width / 2)
			{
				this.preferredLocationLeftMargin = preferredLocation.x;
				this.preferredLocationRightMargin= -1;
			}
			//Otherwise attach it to the right edge of the screen for horizontal positioning
			else
			{
				this.preferredLocationRightMargin = clientDimensions.width - preferredLocation.x;
				this.preferredLocationLeftMargin = -1;
			}

			//If overlay is on top half of the screen attach it to the top edge of the screen for vertical positioning
			if (preferredLocation.y <= clientDimensions.height / 2)
			{
				this.preferredLocationTopMargin = preferredLocation.y;
				this.preferredLocationBottomMargin = -1;
			}
			//Otherwise attach it to the bottom edge of the screen for vertical positioning
			else
			{
				this.preferredLocationBottomMargin = clientDimensions.height - preferredLocation.y;
				this.preferredLocationTopMargin = -1;
			}
		}

		this.preferredLocationUpdated = false;

	}
}
