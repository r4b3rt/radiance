/*
 * Copyright (c) 2005-2021 Radiance Kirill Grouchnikov. All Rights Reserved.
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
package org.pushingpixels.substance.api.text;

import org.pushingpixels.substance.internal.utils.SubstanceTextUtilities;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.text.Format;

/**
 * An extension of the core {@link JFormattedTextField} class tjhat provides consistent support for text selection
 * visuals under different Substance skins and decoration area types.
 */
public class SubstanceFormattedTextField extends JFormattedTextField {
    public SubstanceFormattedTextField() {
    }

    public SubstanceFormattedTextField(Object value) {
        super(value);
    }

    public SubstanceFormattedTextField(Format format) {
        super(format);
    }

    public SubstanceFormattedTextField(AbstractFormatter formatter) {
        super(formatter);
    }

    public SubstanceFormattedTextField(AbstractFormatterFactory factory) {
        super(factory);
    }

    public SubstanceFormattedTextField(AbstractFormatterFactory factory, Object currentValue) {
        super(factory, currentValue);
    }

    @Override
    public Color getSelectionColor() {
        Color base = super.getSelectionColor();
        if (base instanceof UIResource) {
            return SubstanceTextUtilities.getTextSelectionBackground(this);
        } else {
            return base;
        }
    }

    @Override
    public Color getSelectedTextColor() {
        Color base = super.getSelectionColor();
        if (base instanceof UIResource) {
            return SubstanceTextUtilities.getTextSelectionForeground(this);
        } else {
            return base;
        }
    }
}
