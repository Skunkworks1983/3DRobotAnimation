package com.pi.robot.overlay;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Font;
import org.newdawn.slick.TrueTypeFont;

public class TextOverlay {
	public enum Corner {
		UP_LEFT(false, false), UP_RIGHT(true, false), DOWN_LEFT(false, true), DOWN_RIGHT(
				false, false);
		public final boolean right;
		public final boolean bottom;

		private Corner(boolean r, boolean b) {
			this.right = r;
			this.bottom = b;
		}
	}

	private Map<Corner, List<TimedMessage>> messages = new HashMap<Corner, List<TimedMessage>>();
	private Map<Corner, Integer> fixedSizeList = new HashMap<Corner, Integer>();
	private Font font;

	public TextOverlay() {
		for (Corner c : Corner.values()) {
			messages.put(c, new ArrayList<TimedMessage>());
		}
	}

	public void setCornerSize(Corner c, int size) {
		if (size <= 0) {
			fixedSizeList.remove(c);
		} else {
			fixedSizeList.put(c, Integer.valueOf(size));
		}
	}

	public void addMessage(Corner c, TimedMessage message) {
		List<TimedMessage> list = this.messages.get(c);
		
		list.add(message);
		Integer limit = fixedSizeList.get(c);
		if (limit!=null){
			while (list.size() > limit.intValue()) {
				list.remove(0);
			}
		}
	}

	public void renderOverlay(float width, float height) {
		if (font == null) {
			font = new TrueTypeFont(java.awt.Font.decode("Arial").deriveFont(
					14f), true);
		}
		for (Corner corner : messages.keySet()) {
			List<TimedMessage> messages = this.messages.get(corner);
			float y = 0;
			float twidth = 0;
			for (TimedMessage m : messages) {
				twidth = Math.max(twidth, font.getWidth(m.getMessage()));
				y += font.getLineHeight();
			}
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glColor4f(0f, 0f, 0f, 0.5f);
			GL11.glBegin(GL11.GL_QUADS);
			float topX = corner.right ? (width - twidth - 10) : 0;
			float topY = corner.bottom ? (height - y - 10) : 0;
			if (messages.size() > 0) {
				GL11.glVertex3f(topX, topY, -0.0000001f);
				GL11.glVertex3f(topX + twidth + 10, topY, -0.0000001f);
				GL11.glVertex3f(topX + twidth + 10, topY + y + 10, -0.0000001f);
				GL11.glVertex3f(topX, topY + y + 10, -0.0000001f);
			}
			GL11.glEnd();

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glColor3f(1f, 1f, 1f);
			y = 5;
			for (int i = messages.size() - 1; i >= 0; i--) {
				Color c = messages.get(i).getColor();
				if (c != null) {
					GL11.glColor3f(c.getRed() / 255f, c.getGreen() / 255f,
							c.getBlue() / 255f);
				} else {
					GL11.glColor3f(1f, 1f, 1f);
				}
				font.drawString(topX, topY + y, messages.get(i).getMessage());
				if (messages.get(i).isDead() || messages.size() - i > 5) {
					messages.remove(i);
				}
				y += font.getLineHeight();
			}
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}
}
