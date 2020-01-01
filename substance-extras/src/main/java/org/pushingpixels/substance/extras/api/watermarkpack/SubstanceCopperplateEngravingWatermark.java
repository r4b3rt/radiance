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
package org.pushingpixels.substance.extras.api.watermarkpack;

import org.pushingpixels.substance.extras.api.painterpack.noise.CompoundNoiseFilter;
import org.pushingpixels.substance.extras.api.painterpack.noise.FabricFilter;
import org.pushingpixels.substance.extras.api.painterpack.noise.FabricFilter.FabricFilterLink;
import org.pushingpixels.substance.extras.api.painterpack.noise.MedianBeakFilter;
import org.pushingpixels.substance.extras.api.painterpack.noise.NoiseFilter.TrigKind;
import org.pushingpixels.substance.extras.api.painterpack.noise.WoodFilter;

/**
 * Noise-base implementation of {@link
 * org.pushingpixels.substance.api.watermark.SubstanceWatermark}, that imitates copperplate
 * engraving. This class is part of officially supported API.
 * 
 * @author Kirill Grouchnikov
 */
public class SubstanceCopperplateEngravingWatermark extends
		SubstanceNoiseWatermark {
	/**
	 * Creates a new copperplate engraving watermark.
	 */
	public SubstanceCopperplateEngravingWatermark() {
		super("Copperplate Engraving", 0.01, 0.01, false,
				new CompoundNoiseFilter(new WoodFilter(15.0), new FabricFilter(
						FabricFilterLink.getXLink(1.0, 10.0, TrigKind.SINE),
						FabricFilterLink.getYLink(1.0, 10.0, TrigKind.COSINE)),
						new MedianBeakFilter()), true);
	}
}
