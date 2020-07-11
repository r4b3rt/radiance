/*
 * Copyright (c) 2005-2020 Radiance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of the copyright holder nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.flamingo.internal.substance.common.ui;

import org.pushingpixels.flamingo.api.bcb.JBreadcrumbBar;
import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager.CommandButtonSeparatorOrientation;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.model.CommandButtonPresentationModel;
import org.pushingpixels.flamingo.api.common.model.PopupButtonModel;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.internal.substance.common.GlowingResizableIcon;
import org.pushingpixels.flamingo.internal.substance.common.TransitionAwareResizableIcon;
import org.pushingpixels.flamingo.internal.substance.utils.CommandButtonBackgroundDelegate;
import org.pushingpixels.flamingo.internal.substance.utils.CommandButtonVisualStateTracker;
import org.pushingpixels.flamingo.internal.substance.utils.SubstanceDisabledResizableIcon;
import org.pushingpixels.flamingo.internal.ui.common.BasicCommandButtonUI;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;
import org.pushingpixels.flamingo.internal.utils.KeyTipRenderingUtilities;
import org.pushingpixels.neon.api.NeonCortex;
import org.pushingpixels.neon.api.icon.ResizableIcon;
import org.pushingpixels.neon.api.icon.ResizableIconUIResource;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceCortex;
import org.pushingpixels.substance.api.SubstanceSlices;
import org.pushingpixels.substance.api.SubstanceSlices.AnimationFacet;
import org.pushingpixels.substance.api.SubstanceSlices.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.SubstanceSlices.ComponentStateFacet;
import org.pushingpixels.substance.api.colorscheme.SubstanceColorScheme;
import org.pushingpixels.substance.api.painter.border.SubstanceBorderPainter;
import org.pushingpixels.substance.api.painter.fill.SubstanceFillPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;
import org.pushingpixels.substance.api.shaper.SubstanceButtonShaper;
import org.pushingpixels.substance.internal.AnimationConfigurationManager;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker.ModelStateInfo;
import org.pushingpixels.substance.internal.painter.SeparatorPainterUtils;
import org.pushingpixels.substance.internal.utils.*;
import org.pushingpixels.substance.internal.utils.icon.TransitionAware;
import org.pushingpixels.substance.internal.widget.animation.effects.GhostPaintingUtils;
import org.pushingpixels.substance.internal.widget.animation.effects.GhostingListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

/**
 * UI for command buttons {@link JCommandButton} in <b>Substance </b> look and feel.
 *
 * @author Kirill Grouchnikov
 */
public class SubstanceCommandButtonUI extends BasicCommandButtonUI
        implements ActionPopupTransitionAwareUI {
    /**
     * Property change listener. Listens on changes to {@link AbstractButton#MODEL_CHANGED_PROPERTY}
     * property.
     */
    private PropertyChangeListener substancePropertyListener;

    /**
     * Model change listener for ghost image effects.
     */
    private GhostingListener substanceModelChangeListener;

    /**
     * Tracker for visual state transitions.
     */
    private CommandButtonVisualStateTracker substanceVisualStateTracker;

    private ButtonModel overallRolloverModel;

    private RolloverControlListener substanceOverallRolloverListener;

    /**
     * Rollover menu mouse listener.
     */
    private MouseListener rolloverMenuMouseListener;

    private StateTransitionTracker overallStateTransitionTracker;

    /**
     * The matching glowing icon. Is used only when
     * {@link AnimationConfigurationManager#isAnimationAllowed(AnimationFacet, Component)} returns
     * true on {@link AnimationFacet#ICON_GLOW}.
     */
    private GlowingResizableIcon glowingIcon;

    public static ComponentUI createUI(JComponent comp) {
        SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new SubstanceCommandButtonUI((JCommandButton) comp);
    }

    /**
     * Creates a new UI delegate for command button.
     *
     * @param button command button
     */
    SubstanceCommandButtonUI(JCommandButton button) {
        super();

        this.overallRolloverModel = new DefaultButtonModel();
        this.overallRolloverModel.setArmed(false);
        this.overallRolloverModel.setSelected(false);
        this.overallRolloverModel.setPressed(false);
        this.overallRolloverModel.setRollover(false);
        this.overallRolloverModel.setEnabled(button.isEnabled());

        this.overallStateTransitionTracker = new StateTransitionTracker(button,
                this.overallRolloverModel);

        this.substanceVisualStateTracker = new CommandButtonVisualStateTracker();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        SubstanceCortex.ComponentScope.setButtonShaper(this.commandButton,
                ClassicButtonShaper.INSTANCE);

        this.commandButton.setOpaque(false);
    }

    @Override
    protected void updateBorder() {
        Border currBorder = this.commandButton.getBorder();
        if ((currBorder == null) || (currBorder instanceof UIResource)) {
            Insets extra = SubstanceSizeUtils.getDefaultBorderInsets(
                    SubstanceSizeUtils.getComponentFontSize(this.commandButton));
            double hgapScaleFactor = this.commandButton.getHGapScaleFactor();
            double vgapScaleFactor = this.commandButton.getVGapScaleFactor();

            int top = 1 + (int) (vgapScaleFactor * extra.top);
            int left = 2 + (int) (hgapScaleFactor * (1 + extra.left));
            int bottom = 0 + (int) (vgapScaleFactor * extra.bottom);
            int right = 2 + (int) (hgapScaleFactor * (1 + extra.right));
            this.commandButton.setBorder(
                    new BorderUIResource.EmptyBorderUIResource(top, left, bottom, right));
        }
    }

    @Override
    protected void installListeners() {
        super.installListeners();

        this.substanceVisualStateTracker.installListeners(this.commandButton);

        this.substancePropertyListener = (PropertyChangeEvent evt) -> {
            if ("actionModel".equals(evt.getPropertyName())) {
                if (substanceModelChangeListener != null)
                    substanceModelChangeListener.unregisterListeners();
                substanceModelChangeListener = new GhostingListener(commandButton,
                        commandButton.getActionModel());
                substanceModelChangeListener.registerListeners();
            }
            if ("icon".equals(evt.getPropertyName())) {
                trackGlowingIcon();
            }
        };
        this.commandButton.addPropertyChangeListener(this.substancePropertyListener);

        this.substanceModelChangeListener = new GhostingListener(this.commandButton,
                this.commandButton.getActionModel());
        this.substanceModelChangeListener.registerListeners();

        this.substanceOverallRolloverListener = new RolloverControlListener(this,
                this.overallRolloverModel);
        this.commandButton.addMouseListener(this.substanceOverallRolloverListener);
        this.commandButton.addMouseMotionListener(this.substanceOverallRolloverListener);

        this.overallStateTransitionTracker.registerModelListeners();

        this.rolloverMenuMouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (commandButton.isEnabled() && commandButton.getProjection().getPresentationModel().isMenu()) {
                    int modifiers = 0;
                    AWTEvent currentEvent = EventQueue.getCurrentEvent();
                    if (currentEvent instanceof InputEvent) {
                        modifiers = ((InputEvent) currentEvent).getModifiersEx();
                    } else if (currentEvent instanceof ActionEvent) {
                        modifiers = ((ActionEvent) currentEvent).getModifiers();
                    }
                    fireRolloverActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                            commandButton.getActionModel().getActionCommand(),
                            EventQueue.getMostRecentEventTime(), modifiers));

                    // Ignore mouse entered event to initiate displaying the popup content
                    // if the currently displayed popup chain shows global ribbon context menu
                    // that originates from this button.
                    java.util.List<PopupPanelManager.PopupInfo> popupInfoList =
                            PopupPanelManager.defaultManager().getShownPath();
                    int popupInfoListSize = popupInfoList.size();
                    if ((popupInfoListSize >= 1) &&
                            (popupInfoList.get(popupInfoListSize - 1).getPopupPanel()
                                    instanceof JRibbonFrame.GlobalPopupMenu) &&
                            (popupInfoList.get(popupInfoListSize - 1).getPopupOriginator()
                                    == commandButton)) {
                        return;
                    }

                    processPopupAction();
                }
            }
        };
        this.commandButton.addMouseListener(this.rolloverMenuMouseListener);

        this.trackGlowingIcon();
    }

    @Override
    protected void uninstallListeners() {
        this.substanceVisualStateTracker.uninstallListeners(this.commandButton);
        this.substanceVisualStateTracker = null;

        this.commandButton.removePropertyChangeListener(this.substancePropertyListener);
        this.substancePropertyListener = null;

        this.substanceModelChangeListener.unregisterListeners();
        this.substanceModelChangeListener = null;

        this.commandButton.removeMouseListener(this.substanceOverallRolloverListener);
        this.commandButton.removeMouseMotionListener(this.substanceOverallRolloverListener);
        this.substanceOverallRolloverListener = null;

        this.overallStateTransitionTracker.unregisterModelListeners();

        this.commandButton.removeMouseListener(this.rolloverMenuMouseListener);
        this.rolloverMenuMouseListener = null;

        super.uninstallListeners();
    }

    /**
     * Fires the rollover action on all registered handlers.
     *
     * @param e Event object.
     */
    public void fireRolloverActionPerformed(ActionEvent e) {
        // Guaranteed to return a non-null array
        RolloverActionListener[] listeners = commandButton
                .getListeners(RolloverActionListener.class);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 1; i >= 0; i--) {
            (listeners[i]).actionPerformed(e);
        }
    }

    /**
     * Tracks possible usage of glowing icon.
     */
    private void trackGlowingIcon() {
        ResizableIcon currIcon = this.commandButton.getIcon();
        if (currIcon instanceof GlowingResizableIcon)
            return;
        if (currIcon == null)
            return;
        boolean isPopupOnly = ((JCommandButton) this.commandButton)
                .getCommandButtonKind() == JCommandButton.CommandButtonKind.POPUP_ONLY;
        StateTransitionTracker tracker = isPopupOnly
                ? this.substanceVisualStateTracker.getPopupStateTransitionTracker()
                : this.substanceVisualStateTracker.getActionStateTransitionTracker();
        this.glowingIcon = new GlowingResizableIcon(currIcon, tracker.getIconGlowTracker());
    }

    private void paintButtonBackground(Graphics graphics) {
        if (SubstanceCoreUtilities.isButtonNeverPainted(this.commandButton)) {
            return;
        }

        ButtonModel actionModel = this.commandButton.getActionModel();
        PopupButtonModel popupModel = ((JCommandButton) this.commandButton).getPopupModel();
        Rectangle actionArea = this.getLayoutInfo().actionClickArea;
        Rectangle popupArea = this.getLayoutInfo().popupClickArea;

        BufferedImage fullAlphaBackground = CommandButtonBackgroundDelegate
                .getCombinedCommandButtonBackground(this.commandButton, actionModel, actionArea,
                        popupModel, popupArea);

        // Two special cases here:
        // 1. Button has flat appearance, doesn't have focus and doesn't show the popup
        // 2. Button is disabled.
        // For both cases, we need to set custom translucency.
        boolean isFlat = this.commandButton.isFlat()
                && !this.commandButton.hasFocus()
                && !((JCommandButton) this.commandButton).getPopupModel().isPopupShowing();
        boolean isSpecial = isFlat || !this.commandButton.isEnabled();
        float extraAlpha = 1.0f;
        if (isSpecial) {
            if (isFlat) {
                float extraActionAlpha = 0.0f;
                for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry :
                        getActionTransitionTracker().getModelStateInfo().getStateContributionMap().entrySet()) {
                    ComponentState activeState = activeEntry.getKey();
                    if (activeState.isDisabled()) {
                        continue;
                    }
                    if (activeState == ComponentState.ENABLED) {
                        continue;
                    }
                    extraActionAlpha += activeEntry.getValue().getContribution();
                }
                float extraPopupAlpha = 0.0f;
                for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry :
                        getPopupTransitionTracker().getModelStateInfo().getStateContributionMap().entrySet()) {
                    ComponentState activeState = activeEntry.getKey();
                    if (activeState.isDisabled()) {
                        continue;
                    }
                    if (activeState == ComponentState.ENABLED) {
                        continue;
                    }
                    extraPopupAlpha += activeEntry.getValue().getContribution();
                }
                extraAlpha = Math.max(extraActionAlpha, extraPopupAlpha);
            } else {
                ComponentState actionAreaState = ComponentState.getState(actionModel,
                        this.commandButton);
                if (actionAreaState.isDisabled()) {
                    extraAlpha = SubstanceColorSchemeUtilities.getAlpha(this.commandButton,
                            actionAreaState);
                }
            }
        }
        // System.out.println(extraAlpha);
        extraAlpha = Math.min(1.0f, extraAlpha);
        if (extraAlpha > 0.0f) {
            Graphics2D g2d = (Graphics2D) graphics.create();
            g2d.setComposite(
                    WidgetUtilities.getAlphaComposite(this.commandButton, extraAlpha, graphics));
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            NeonCortex.drawImage(g2d, fullAlphaBackground, 0, 0);
            g2d.dispose();
        }
    }

    private void paintButtonIconRegular(Graphics g, Rectangle iconRect) {
        JCommandButton jcb = (JCommandButton) this.commandButton;
        Icon regular = jcb.getIcon();
        if (toUseDisabledIcon() && (jcb.getDisabledIcon() != null) && ((regular != null)
                && !regular.getClass().isAnnotationPresent(TransitionAware.class))) {
            regular = jcb.getDisabledIcon();
        }

        if ((iconRect == null) || (regular == null) || (iconRect.width == 0)
                || (iconRect.height == 0)) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();

        GhostPaintingUtils.paintGhostIcon(g2d, jcb, regular, iconRect);
        g2d.setComposite(WidgetUtilities.getAlphaComposite(jcb, g));

        StateTransitionTracker tracker = this.substanceVisualStateTracker
                .getActionStateTransitionTracker();
        ButtonModel model = commandButton.getActionModel();
        if (jcb.getCommandButtonKind() == CommandButtonKind.POPUP_ONLY) {
            tracker = this.substanceVisualStateTracker.getPopupStateTransitionTracker();
            model = jcb.getPopupModel();
        }
        CommandButtonBackgroundDelegate.paintCommandButtonIcon(g2d, iconRect, jcb, regular,
                this.glowingIcon, model, tracker);
        g2d.dispose();
    }

    protected void paintButtonIcon(Graphics g, Rectangle iconRect) {
        boolean isSelectedMenu = this.commandButton.getActionModel().isSelected() &&
                this.commandButton.getProjection().getPresentationModel().isMenu();
        if (isSelectedMenu) {
            Graphics2D g2d = (Graphics2D) g.create();
            float borderDelta = SubstanceSizeUtils.getBorderStrokeWidth();
            Rectangle2D.Float extended = new Rectangle2D.Float(iconRect.x - borderDelta / 2.0f,
                    iconRect.y - borderDelta / 2.0f, iconRect.width + borderDelta,
                    iconRect.height + borderDelta);

            ComponentState currState = this.commandButton.getActionModel().isEnabled()
                    ? ComponentState.SELECTED
                    : ComponentState.DISABLED_SELECTED;

            SubstanceColorScheme fillScheme = SubstanceColorSchemeUtilities.getColorScheme(
                    this.commandButton, SubstanceSlices.ColorSchemeAssociationKind.HIGHLIGHT, currState);
            SubstanceFillPainter fillPainter = SubstanceCoreUtilities
                    .getFillPainter(this.commandButton);
            fillPainter.paintContourBackground(g2d, this.commandButton,
                    extended.x + extended.width, extended.y + extended.height,
                    extended, false, fillScheme, false);

            SubstanceColorScheme borderScheme = SubstanceColorSchemeUtilities.getColorScheme(
                    this.commandButton, SubstanceSlices.ColorSchemeAssociationKind.HIGHLIGHT_BORDER, currState);
            SubstanceBorderPainter borderPainter = SubstanceCoreUtilities
                    .getBorderPainter(this.commandButton);
            borderPainter.paintBorder(g2d, this.commandButton,
                    extended.x + extended.width, extended.y + extended.height,
                    extended, null, borderScheme);

            g2d.dispose();
        }
        this.paintButtonIconRegular(g, iconRect);
        // does it actually have an icon?
        Icon iconToPaint = this.getIconToPaint();
        if (isSelectedMenu && (iconToPaint == null)) {
            // draw a checkmark
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            ComponentState currState = this.commandButton.getActionModel().isEnabled()
                    ? ComponentState.SELECTED
                    : ComponentState.DISABLED_SELECTED;
            SubstanceColorScheme fillScheme = SubstanceColorSchemeUtilities.getColorScheme(
                    this.commandButton, SubstanceSlices.ColorSchemeAssociationKind.HIGHLIGHT, currState);
            g2d.setColor(fillScheme.getForegroundColor());

            int iw = iconRect.width;
            int ih = iconRect.height;
            GeneralPath path = new GeneralPath();

            path.moveTo(0.2f * iw, 0.5f * ih);
            path.lineTo(0.42f * iw, 0.8f * ih);
            path.lineTo(0.8f * iw, 0.2f * ih);
            g2d.translate(iconRect.x, iconRect.y);
            Stroke stroke = new BasicStroke((float) 0.12 * iw, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND);
            g2d.setStroke(stroke);
            g2d.draw(path);

            g2d.dispose();
        }
    }

    private void paintButtonHorizontalSeparator(Graphics graphics, Rectangle separatorArea) {
        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.translate(0, separatorArea.y);

        SubstanceColorScheme colorScheme = SubstanceColorSchemeUtilities.getColorScheme(
                this.commandButton, ColorSchemeAssociationKind.SEPARATOR,
                ComponentState.getState(this.commandButton.getActionModel(), this.commandButton));

        float fadeAlpha = this.getSeparatorAlpha();
        g2d.setComposite(AlphaComposite.SrcOver.derive(fadeAlpha));

        SeparatorPainterUtils.paintSeparator(this.commandButton, g2d, colorScheme,
                this.commandButton.getWidth(), 1, JSlider.HORIZONTAL, true, 4, 4, true);

        g2d.dispose();
    }

    private void paintButtonVerticalSeparator(Graphics graphics, Rectangle separatorArea) {
        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.translate(separatorArea.x, 0);

        SubstanceColorScheme colorScheme = SubstanceColorSchemeUtilities.getColorScheme(
                this.commandButton, ColorSchemeAssociationKind.SEPARATOR,
                ComponentState.getState(this.commandButton.getActionModel(), this.commandButton));

        float fadeAlpha = this.getSeparatorAlpha();
        g2d.setComposite(AlphaComposite.SrcOver.derive(fadeAlpha));

        SeparatorPainterUtils.paintSeparator(this.commandButton, g2d, colorScheme, 1,
                this.commandButton.getHeight(), JSlider.VERTICAL, true, 4, 4, true);

        g2d.dispose();
    }

    @Override
    protected boolean isPaintingBackground() {
        if (this.commandButton.getProjection().getPresentationModel().isMenu() &&
                (this.commandButton.getActionModel().isRollover() || !this.commandButton.isFlat())) {
            return true;
        }

        if (super.isPaintingBackground()) {
            return true;
        }
        return this.commandButton.hasFocus() || (this.overallStateTransitionTracker
                .getFacetStrength(ComponentStateFacet.ROLLOVER) > 0.0f);
    }

    @Override
    protected boolean isPaintingSeparators() {
        if (super.isPaintingSeparators()) {
            return true;
        }
        boolean hasIcon = (this.commandButton.getIcon() != null);
        return this.commandButton.hasFocus() || (hasIcon && (this.overallStateTransitionTracker
                .getFacetStrength(ComponentStateFacet.ROLLOVER) > 0.0f));
    }

    @Override
    protected ResizableIcon createPopupActionIcon() {
        final int fontSize = SubstanceSizeUtils.getComponentFontSize(this.commandButton);
        int arrowIconHeight = (int) SubstanceSizeUtils.getArrowIconHeight(fontSize);
        int arrowIconWidth = (int) SubstanceSizeUtils.getArrowIconWidth(fontSize);
        return new TransitionAwareResizableIcon(this.commandButton,
                this::getPopupTransitionTracker,
                (scheme, width, height) -> {
                    CommandButtonPresentationModel.PopupOrientationKind orientation =
                            commandButton.getPopupOrientationKind();
                    int direction =
                            (orientation == CommandButtonPresentationModel.PopupOrientationKind.DOWNWARD)
                                    ? SwingConstants.SOUTH
                                    : (commandButton.getComponentOrientation().isLeftToRight()
                                    ? SwingConstants.EAST : SwingConstants.WEST);
                    // System.out.println(direction + ":" + width + ":"
                    // + height);
                    return SubstanceImageCreator.getArrowIcon(width,
                            height, SubstanceSizeUtils.getArrowStrokeWidth(fontSize) - 0.5f,
                            direction, scheme);
                    // System.out.println(" --> " + result.getIconWidth()
                    // + "*" + result.getIconHeight());
                }, new Dimension(arrowIconWidth, arrowIconHeight));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setFont(this.commandButton.getFont());

        this.layoutInfo = this.layoutManager.getLayoutInfo(this.commandButton);
        commandButton.putClientProperty("icon.bounds", layoutInfo.iconRect);

        if (this.isPaintingBackground()) {
            this.paintButtonBackground(g2d);
        }

        // decide which command button model should be used to
        // compute the foreground color of the command button's text
        boolean useActionAreaForFg = layoutInfo.isTextInActionArea;
        StateTransitionTracker transitionTrackerForFg = useActionAreaForFg
                ? this.getActionTransitionTracker()
                : this.getPopupTransitionTracker();
        ModelStateInfo modelStateInfoForFg = transitionTrackerForFg.getModelStateInfo();
        ComponentState currStateForFg = modelStateInfoForFg.getCurrModelState();
        Color fgColor = getForegroundColor(modelStateInfoForFg);

        if (layoutInfo.textLayoutInfoList != null) {
            for (CommandButtonLayoutManager.TextLayoutInfo mainTextLayoutInfo :
                    layoutInfo.textLayoutInfoList) {
                if (mainTextLayoutInfo.text != null) {
                    SubstanceTextUtilities.paintText(g2d, c, mainTextLayoutInfo.textRect,
                            mainTextLayoutInfo.text, -1, g2d.getFont(), fgColor,
                            g2d.getClipBounds());
                }
            }
        }

        if (layoutInfo.extraTextLayoutInfoList != null) {
            Color disabledFgColor = SubstanceColorSchemeUtilities.getColorScheme(
                    this.commandButton, ComponentState.DISABLED_UNSELECTED).getForegroundColor();
            float buttonAlpha = SubstanceColorSchemeUtilities.getAlpha(this.commandButton,
                    ComponentState.DISABLED_UNSELECTED);
            if (buttonAlpha < 1.0f) {
                Color bgFillColor = SubstanceColorUtilities.getBackgroundFillColor(
                        this.commandButton);
                disabledFgColor = SubstanceColorUtilities.getInterpolatedColor(disabledFgColor,
                        bgFillColor, buttonAlpha);
            }
            if (currStateForFg.isDisabled()) {
                disabledFgColor = SubstanceColorUtilities.getInterpolatedColor(disabledFgColor,
                        SubstanceColorUtilities.getBackgroundFillColor(c), 0.5);
            }
            disabledFgColor = SubstanceColorUtilities.getInterpolatedColor(disabledFgColor,
                    fgColor, 0.5);
            for (CommandButtonLayoutManager.TextLayoutInfo extraTextLayoutInfo :
                    layoutInfo.extraTextLayoutInfoList) {
                if (extraTextLayoutInfo.text != null) {
                    SubstanceTextUtilities.paintText(g2d, c, extraTextLayoutInfo.textRect,
                            extraTextLayoutInfo.text, -1, g2d.getFont(), disabledFgColor,
                            g2d.getClipBounds());
                }
            }
        }

        if (layoutInfo.iconRect != null) {
            this.paintButtonIcon(g2d, layoutInfo.iconRect);
        }
        if (layoutInfo.popupActionRect.getWidth() > 0) {
            paintPopupActionIcon(g2d, layoutInfo.popupActionRect);
        }

        if (this.isPaintingSeparators() && (layoutInfo.separatorArea != null)) {
            if (layoutInfo.separatorOrientation == CommandButtonSeparatorOrientation.HORIZONTAL) {
                this.paintButtonHorizontalSeparator(g2d, layoutInfo.separatorArea);
            } else {
                this.paintButtonVerticalSeparator(g2d, layoutInfo.separatorArea);
            }
        }

        float focusRingPadding = SubstanceSizeUtils.getFocusRingPadding(SubstanceSizeUtils
                .getComponentFontSize(this.commandButton));
        Rectangle innerFocusArea = this.isInnerFocusOnAction ? layoutInfo.actionClickArea
                : layoutInfo.popupClickArea;
        Shape insetFocusArea = new Rectangle2D.Float(
                innerFocusArea.x + focusRingPadding,
                innerFocusArea.y + focusRingPadding,
                innerFocusArea.width - 2 * focusRingPadding,
                innerFocusArea.height - 2 * focusRingPadding);
        SubstanceCoreUtilities.paintFocus(g2d, this.commandButton, this.commandButton, this,
                insetFocusArea, innerFocusArea, 1.0f, 0);

        // g2d.setColor(Color.red);
        // g2d.draw(layoutInfo.iconRect);
        // g2d.setColor(Color.blue);
        // if (layoutInfo.textLayoutInfoList != null) {
        // for (CommandButtonLayoutManager.TextLayoutInfo mainTextLayoutInfo :
        // layoutInfo.textLayoutInfoList) {
        // if (mainTextLayoutInfo.text != null) {
        // g2d.draw(mainTextLayoutInfo.textRect);
        // }
        // }
        // }
        // g2d.setColor(Color.magenta);
        // if (layoutInfo.extraTextLayoutInfoList != null) {
        // for (CommandButtonLayoutManager.TextLayoutInfo extraTextLayoutInfo :
        // layoutInfo.extraTextLayoutInfoList) {
        // if (extraTextLayoutInfo.text != null) {
        // g2d.draw(extraTextLayoutInfo.textRect);
        // }
        // }
        // }
        // g2d.setColor(Color.green);
        // g2d.draw(layoutInfo.popupActionRect);

        g2d.dispose();
    }

    protected Color getForegroundColor(ModelStateInfo modelStateInfo) {
        Color fgColor = this.commandButton.getForeground();
        if (fgColor instanceof UIResource) {
            float buttonAlpha = SubstanceColorSchemeUtilities.getAlpha(this.commandButton,
                    modelStateInfo.getCurrModelState());

            if (this.commandButton.getProjection().getPresentationModel().isMenu()) {
                fgColor = getMenuButtonForegroundColor(this.commandButton, modelStateInfo);

                if (buttonAlpha < 1.0f) {
                    Color bgFillColor = SubstanceColorUtilities
                            .getBackgroundFillColor(this.commandButton);
                    fgColor = SubstanceColorUtilities.getInterpolatedColor(fgColor, bgFillColor,
                            buttonAlpha);
                }
            } else {
                fgColor = SubstanceTextUtilities.getForegroundColor(this.commandButton,
                        this.commandButton.getText(), modelStateInfo, buttonAlpha);
            }
        }
        return fgColor;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g.create();
        NeonCortex.installDesktopHints(g2d, this.commandButton.getFont());
        this.paint(g2d, c);

        if (this.commandButton.getProjection().getPresentationModel().isMenu()) {
            KeyTipRenderingUtilities.renderButtonKeyTips(g, this.commandButton, layoutManager);
        }

        g2d.dispose();
    }

    private void paintPopupActionIcon(Graphics g, Rectangle popupActionRect) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        popupActionIcon.paintIcon(this.commandButton, g2d,
                popupActionRect.x + (popupActionRect.width - popupActionIcon.getIconWidth()) / 2,
                popupActionRect.y + (popupActionRect.height - popupActionIcon.getIconHeight()) / 2);
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        JCommandButton button = (JCommandButton) c;
        SubstanceButtonShaper shaper = SubstanceCoreUtilities.getButtonShaper(button);

        Dimension superPref = super.getPreferredSize(button);
        if (superPref == null)
            return null;

        if (shaper == null)
            return superPref;

        // fix for issue 35 on Flamingo - do not enforce
        // min size on buttons in the ribbon
        // Additional fix - buttons with popup action should
        // not have min size enforced as well
        // Additional fix - buttons in popup menus and breadcrumb bars should
        // not have min size enforced
        if ((button.getPresentationState() == CommandButtonPresentationState.MEDIUM)
                && (SwingUtilities.getAncestorOfClass(JRibbon.class, button) == null)
                && (SwingUtilities.getAncestorOfClass(JBreadcrumbBar.class, button) == null)
                && (SwingUtilities.getAncestorOfClass(JCommandPopupMenu.class, button) == null)) {
            JButton dummy = new JButton(button.getText(), button.getIcon());
            Dimension result = shaper.getPreferredSize(dummy, superPref);
            if (FlamingoUtilities.hasPopupAction(button)) {
                result.width = superPref.width;
            }
            return result;
        }
        return superPref;
    }

    /**
     * Computes the alpha value for painting the separators.
     *
     * @return Alpha value for painting the separators.
     */
    private float getSeparatorAlpha() {
        ComponentState actionAreaState = this.getActionTransitionTracker().getModelStateInfo()
                .getCurrModelState();

        if (!actionAreaState.isFacetActive(ComponentStateFacet.SELECTION)
                && !actionAreaState.isDisabled()) {
            float actionRolloverCycle = this.getActionTransitionTracker()
                    .getFacetStrength(ComponentStateFacet.ROLLOVER);
            float popupRolloverCycle = this.getPopupTransitionTracker()
                    .getFacetStrength(ComponentStateFacet.ROLLOVER);
            return Math.min(1.0f, actionRolloverCycle + popupRolloverCycle);
        }
        return 1.0f;
    }

    @Override
    protected void syncDisabledIcon() {
        ResizableIcon currDisabledIcon = this.commandButton.getDisabledIcon();
        ResizableIcon icon = this.commandButton.getIcon();
        if ((currDisabledIcon == null) || ((currDisabledIcon instanceof UIResource)
                && !currDisabledIcon.getClass().isAnnotationPresent(TransitionAware.class))) {
            if (icon != null) {
                this.commandButton.setDisabledIcon(
                        new ResizableIconUIResource(new SubstanceDisabledResizableIcon(icon)));
            } else {
                this.commandButton.setDisabledIcon(null);
            }
        } else {
            // disabled icon coming from app code
            if (icon != null) {
                this.commandButton.getDisabledIcon()
                        .setDimension(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            }
        }
    }

    @Override
    public StateTransitionTracker getTransitionTracker() {
        return this.overallStateTransitionTracker;
    }

    @Override
    public boolean isInside(MouseEvent me) {
        boolean inAction = (this.layoutInfo.actionClickArea != null)
                && this.layoutInfo.actionClickArea.contains(me.getPoint());
        boolean inPopup = (this.layoutInfo.popupClickArea != null)
                && this.layoutInfo.popupClickArea.contains(me.getPoint());
        return inAction || inPopup;
    }

    @Override
    public StateTransitionTracker getActionTransitionTracker() {
        return this.substanceVisualStateTracker.getActionStateTransitionTracker();
    }

    @Override
    public StateTransitionTracker getPopupTransitionTracker() {
        return this.substanceVisualStateTracker.getPopupStateTransitionTracker();
    }


    private static Color getMenuButtonForegroundColor(JCommandButton menuButton,
            StateTransitionTracker.ModelStateInfo modelStateInfo) {
        ComponentState currState = modelStateInfo.getCurrModelStateNoSelection();
        Map<ComponentState, StateTransitionTracker.StateContributionInfo> activeStates = modelStateInfo
                .getStateNoSelectionContributionMap();

        SubstanceSlices.ColorSchemeAssociationKind currAssocKind = SubstanceSlices.ColorSchemeAssociationKind.FILL;
        // use HIGHLIGHT on active and non-rollover menu items
        if (!currState.isDisabled() && (currState != ComponentState.ENABLED)
                && !currState.isFacetActive(SubstanceSlices.ComponentStateFacet.ROLLOVER))
            currAssocKind = SubstanceSlices.ColorSchemeAssociationKind.HIGHLIGHT;
        SubstanceColorScheme colorScheme = SubstanceColorSchemeUtilities.getColorScheme(menuButton,
                currAssocKind, currState);
        if (currState.isDisabled() || (activeStates == null) || (activeStates.size() == 1)) {
            return colorScheme.getForegroundColor();
        }

        float aggrRed = 0;
        float aggrGreen = 0;
        float aggrBlue = 0;
        for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : activeStates
                .entrySet()) {
            ComponentState activeState = activeEntry.getKey();
            float alpha = activeEntry.getValue().getContribution();
            SubstanceSlices.ColorSchemeAssociationKind assocKind = SubstanceSlices.ColorSchemeAssociationKind.FILL;
            // use HIGHLIGHT on active and non-rollover menu items
            if (!activeState.isDisabled() && (activeState != ComponentState.ENABLED)
                    && !activeState.isFacetActive(SubstanceSlices.ComponentStateFacet.ROLLOVER))
                assocKind = SubstanceSlices.ColorSchemeAssociationKind.HIGHLIGHT;
            SubstanceColorScheme activeColorScheme = SubstanceColorSchemeUtilities
                    .getColorScheme(menuButton, assocKind, activeState);
            Color activeForeground = activeColorScheme.getForegroundColor();
            aggrRed += alpha * activeForeground.getRed();
            aggrGreen += alpha * activeForeground.getGreen();
            aggrBlue += alpha * activeForeground.getBlue();
        }
        return new Color((int) aggrRed, (int) aggrGreen, (int) aggrBlue);
    }
}
