/* Copyright 2002-2017 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.estimation.measurements;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.data.BodiesElements;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;

/** Interface for computing reference points displacement.
 * @author Luc Maisonobe
 * @since 9.1
 */
public interface StationDisplacement {

    /** Compute displacement of a ground reference point.
     * @param elements elements affecting Earth orientation
     * @param earthFrame Earth frame in which reference point is defined
     * @param referencePoint reference point position in {@code earthFrame}
     * @return displacement vector to be <em>added</em> to {@code referencePoint}
     * @exception OrekitException if displacement cannot be computed
     */
    Vector3D displacement(BodiesElements elements, Frame earthFrame, Vector3D referencePoint)
        throws OrekitException;

}
