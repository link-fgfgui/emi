package dev.emi.emi.screen.widget.config;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;

public class ListWidget extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
	private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = EmiPort.id("minecraft", "textures/gui/menu_list_background.png");
	private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = EmiPort.id("minecraft", "textures/gui/inworld_menu_list_background.png");

	protected final Minecraft client;
	private final List<Entry> children = Lists.newArrayList();
	protected int width;
	protected int height;
	protected int top;
	protected int bottom;
	protected int right;
	protected int left;
	private double scrollAmount;
	private boolean renderSelection = true;
	private boolean scrolling;
	private Entry selected;
	private Entry hoveredEntry;
	public int padding = 4;

	public ListWidget(Minecraft client, int width, int height, int top, int bottom) {
		this.client = client;
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.left = 0;
		this.right = width;
	}

	public void setRenderSelection(boolean renderSelection) {
		this.renderSelection = renderSelection;
	}

	public int getRowWidth() {
		return Math.min(400, width - 60);
	}

	public int getLogicalHeight() {
		return bottom - top;
	}

	@Nullable
	public Entry getSelectedOrNull() {
		return this.selected;
	}

	public void setSelected(@Nullable Entry entry) {
		this.selected = entry;
	}

	public final List<Entry> children() {
		return this.children;
	}

	protected final void clearEntries() {
		this.children.clear();
	}

	protected void replaceEntries(Collection<Entry> newEntries) {
		this.children.clear();
		this.children.addAll(newEntries);
	}

	protected Entry getEntry(int index) {
		return this.children().get(index);
	}

	public int addEntry(Entry entry) {
		this.children.add(entry);
		entry.parentList = this;
		return this.children.size() - 1;
	}

	protected int getEntryCount() {
		return this.children().size();
	}

	protected boolean isSelectedEntry(int index) {
		return Objects.equals(this.getSelectedOrNull(), this.children().get(index));
	}

	@Nullable
	protected final Entry getEntryAtPosition(double x, double y) {
		int rowWidth = this.getRowWidth() / 2;
		int mid = this.left + this.width / 2;
		int rowLeft = mid - rowWidth;
		int rowRight = mid + rowWidth;
		int m = Mth.floor(y - (double)this.top) + (int)this.getScrollAmount() - 4;
		if (x < this.getScrollbarPositionX() && x >= rowLeft && x <= rowRight && m >= 0) {
			int h = 0;
			for (int i = 0; i < this.getEntryCount(); i++) {
				int eh = getEntryHeight(i);
				if (m >= h && m < h + eh - padding) {
					return this.getEntry(i);
				}
				h += eh;
			}
		}
		return null;
	}

	public void updateSize(int width, int height, int top, int bottom) {
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.left = 0;
		this.right = width;
	}

	public void setLeftPos(int left) {
		this.left = left;
		this.right = left + this.width;
	}

	protected int getMaxPosition() {
		return this.getTotalHeight();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
		int o;
		int n;
		int m;
		int i = this.getScrollbarPositionX();
		int j = i + 6;
		this.hoveredEntry = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

		{
			Identifier identifier = this.client.level == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
			draw.blit(RenderPipelines.GUI_TEXTURED, identifier, left, top, 0.0F, (float)scrollAmount, right - left, bottom - top, 32, 32);
		}

		draw.enableScissor(left, top, right, bottom);
		int k = this.getRowLeft();
		int l = this.top + 4 - (int)this.getScrollAmount();
		this.renderList(draw, k, l, mouseX, mouseY, delta);
		draw.disableScissor();


		{
			Identifier identifier = this.client.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
			Identifier identifier2 = this.client.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
			draw.blit(RenderPipelines.GUI_TEXTURED, identifier, left, top - 2, 0.0F, 0.0F, width, 2, 32, 2);
			draw.blit(RenderPipelines.GUI_TEXTURED, identifier2, left, bottom, 0.0F, 0.0F, width, 2, 32, 2);
		}

		if ((o = this.getMaxScroll()) > 0) {
			m = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
			m = Mth.clamp(m, 32, this.bottom - this.top - 8);
			n = (int)this.getScrollAmount() * (this.bottom - this.top - m) / o + this.top;
			if (n < this.top) {
				n = this.top;
			}
			draw.fill(RenderPipelines.GUI, i, this.bottom, j, this.top, 0xFF000000);
			draw.fill(RenderPipelines.GUI, i, n + m, j, n, 0xFF808080);
			draw.fill(RenderPipelines.GUI, i, n + m - 1, j - 1, n, 0xFFC0C0C0);
		}
	}

	public void centerScrollOn(Entry entry) {
		int i = 0;
		for (Entry e : this.children()) {
			if (e == entry) {
				this.setScrollAmount(i - 42);
				return;
			}
			i += getEntryHeight(e);
		}
	}

	protected void ensureVisible(Entry entry) {
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.top - 4 - entry.getHeight();
		int k = this.bottom - i - entry.getHeight() * 2;
		if (j < 0) {
			this.scroll(j);
		}
		if (k < 0) {
			this.scroll(-k);
		}
	}

	private void scroll(int amount) {
		this.setScrollAmount(this.getScrollAmount() + (double)amount);
	}

	public double getScrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double amount) {
		this.scrollAmount = Mth.clamp(amount, 0.0, (double)this.getMaxScroll());
	}

	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4) + 40);
	}

	protected void updateScrollingState(MouseButtonEvent event) {
		this.scrolling = event.button() == 0 && event.x() >= (double)this.getScrollbarPositionX() && event.x() < (double)(this.getScrollbarPositionX() + 6);
	}

	protected int getScrollbarPositionX() {
		return this.width - 6;
	}

	public void unfocusTextField() {
		for (Entry e : this.children) {
			for (GuiEventListener el : e.children()) {
				if (el instanceof EditBox tfw) {
					EmiPort.focus(tfw, false);
				}
			}
		}
	}

	public EditBox getFocusedTextField() {
		for (Entry e : this.children) {
			for (GuiEventListener el : e.children()) {
				if (el instanceof EditBox tfw) {
					if (tfw.isFocused()) {
						return tfw;
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		this.updateScrollingState(event);
		unfocusTextField();
		if (!this.isMouseOver(event.x(), event.y())) {
			return false;
		}
		Entry entry = this.getEntryAtPosition(event.x(), event.y());
		if (entry != null) {
			if (entry.mouseClicked(event, doubleClick)) {
				this.setFocused((GuiEventListener)entry);
				this.setDragging(true);
				return true;
			}
		}
		return this.scrolling;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.getFocused() != null) {
			this.getFocused().mouseReleased(event);
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (super.mouseDragged(event, deltaX, deltaY)) {
			return true;
		}
		if (event.button() != 0 || !this.scrolling) {
			return false;
		}
		if (event.y() < (double)this.top) {
			this.setScrollAmount(0.0);
		} else if (event.y() > (double)this.bottom) {
			this.setScrollAmount(this.getMaxScroll());
		} else {
			double d = Math.max(1, this.getMaxScroll());
			int i = this.bottom - this.top;
			int j = Mth.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
			double e = Math.max(1.0, d / (double)(i - j));
			this.setScrollAmount(this.getScrollAmount() + deltaY * e);
		}
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double amount) {
		this.setScrollAmount(this.getScrollAmount() - amount * 22);
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event)) {
			return true;
		}
		return false;
	}

	protected void moveSelection(MoveDirection direction) {
		this.moveSelectionIf(direction, entry -> true);
	}

	protected void ensureSelectedEntryVisible() {
		Entry entry = this.getSelectedOrNull();
		if (entry != null) {
			this.setSelected(entry);
			this.ensureVisible(entry);
		}
	}

	protected void moveSelectionIf(MoveDirection direction, Predicate<Entry> predicate) {
		int i = direction == MoveDirection.UP ? -1 : 1;
		if (!this.children().isEmpty()) {
			int k;
			int j = this.children().indexOf(this.getSelectedOrNull());
			while (j != (k = Mth.clamp(j + i, 0, this.getEntryCount() - 1))) {
				Entry entry = (Entry)this.children().get(k);
				if (predicate.test(entry)) {
					this.setSelected(entry);
					this.ensureVisible(entry);
					break;
				}
				j = k;
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseY >= (double)this.top && mouseY <= (double)this.bottom && mouseX >= (double)this.left && mouseX <= (double)this.right;
	}

	protected void renderList(GuiGraphicsExtractor draw, int x, int y, int mouseX, int mouseY, float delta) {
		int i = this.getEntryCount();
		for (int j = 0; j < i; ++j) {
			int p;
			int k = this.getRowTop(j);
			int l = this.getRowBottom(j);
			if (l < this.top || k > this.bottom) continue;
			int m = k;
			int n = getEntryHeight(j);
			if (n == 0) {
				continue;
			}
			n -= 4;
			Entry entry = this.getEntry(j);
			int o = this.getRowWidth();
			if (this.renderSelection && this.isSelectedEntry(j)) {
				p = this.left + this.width / 2 - o / 2;
				int q = this.left + this.width / 2 + o / 2;
				float f = this.isFocused() ? 1.0f : 0.5f;
				draw.fill(RenderPipelines.GUI, p, m + n + 2, q, m - 2, Mth.ceil(f * 255.0F) << 24);
				draw.fill(RenderPipelines.GUI, p + 1, m + n + 1, q - 1, m - 1, 0xFF000000);
			}
			p = this.getRowLeft();
			((Entry)entry).render(draw, j, k, p, o - 3, n, mouseX, mouseY, Objects.equals(this.hoveredEntry, entry), delta);
		}
	}

	public int getRowLeft() {
		return this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
	}

	public int getRowRight() {
		return this.getRowLeft() + this.getRowWidth();
	}

	private int getEntryHeight(int i) {
		return getEntryHeight(this.getEntry(i));
	}

	private int getEntryHeight(Entry entry) {
		int h = entry.getHeight();
		if (h == 0) {
			return 0;
		}
		return h + padding;
	}

	protected int getRowTop(int index) {
		int height = 0;
		for (int i = 0; i < index; i++) {
			height += getEntryHeight(i);
		}
		return this.top + 4 - (int)this.getScrollAmount() + height;
	}

	private int getRowBottom(int index) {
		return this.getRowTop(index) + this.getEntry(index).getHeight();
	}

	public boolean isFocused() {
		return false;
	}

	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		if (this.isFocused()) {
			return NarratableEntry.NarrationPriority.FOCUSED;
		}
		if (this.hoveredEntry != null) {
			return NarratableEntry.NarrationPriority.HOVERED;
		}
		return NarratableEntry.NarrationPriority.NONE;
	}

	@Nullable
	public Entry getHoveredEntry() {
		return this.hoveredEntry;
	}

	void setEntryParentList(Entry entry) {
		entry.parentList = this;
	}

	protected void appendNarrations(NarrationElementOutput builder, Entry entry) {
		int i;
		List<Entry> list = this.children();
		if (list.size() > 1 && (i = list.indexOf(entry)) != -1) {
			builder.add(NarratedElementType.POSITION, (Component)EmiPort.translatable("narrator.position.list", i + 1, list.size()));
		}
	}

	public int getTotalHeight() {
		int height = 0;
		for (int i = 0; i < this.getEntryCount(); i++) {
			height += getEntryHeight(i);
		}
		if (height > 0) {
			height -= padding;
		}
		return height;
	}

	@Override
	public void updateNarration(NarrationElementOutput var1) {
	}

	public static abstract class Entry extends AbstractContainerEventHandler {
		public ListWidget parentList;

		public abstract void render(GuiGraphicsExtractor draw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta);

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return Objects.equals(this.parentList.getEntryAtPosition(mouseX, mouseY), this);
		}

		public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
			return List.of();
		}

		public abstract int getHeight();
	}

	protected static enum MoveDirection {
		UP,
		DOWN;

	}
}
