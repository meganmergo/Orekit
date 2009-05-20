/* Copyright 2002-2008 CS Communication & Systèmes
 * Licensed to CS Communication & Systèmes (CS) under one or more
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
package org.orekit.frames;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.math.geometry.Vector3D;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.UTCScale;
import org.orekit.utils.PVCoordinates;


public class PEFFrameTest extends TestCase {

    public void testAASReferenceLEO() throws OrekitException {

        // this reference test has been extracted from the following paper:
        // Implementation Issues Surrounding the New IAU Reference Systems for Astrodynamics
        // David A. Vallado, John H. Seago, P. Kenneth Seidelmann
        // http://www.centerforspace.com/downloads/files/pubs/AAS-06-134.pdf
        AbsoluteDate t0 = new AbsoluteDate(new DateComponents(2004, 04, 06),
                                           new TimeComponents(07, 51, 28.386009),
                                           UTCScale.getInstance());

// PEF iau76
        PVCoordinates pvPEF =
           new PVCoordinates(new Vector3D(-1033475.0313, 7901305.5856, 6380344.5328),
                             new Vector3D(-3225.632747, -2872.442511, 5531.931288));
// TOD iau76
        PVCoordinates pvTEME =
            new PVCoordinates(new Vector3D(5094514.7804, 6127366.4612, 6380344.5328),
                              new Vector3D(-4746.088567, 786.077222, 5531.931288));
        
        TEMEFrame TEMEframe = new TEMEFrame(true, AbsoluteDate.J2000_EPOCH, "TEME w corr");
        PEFFrame  PEFframe  = new PEFFrame(true, AbsoluteDate.J2000_EPOCH, "PEF w corr");

        Transform tt = TEMEframe.getTransformTo(PEFframe, t0);
        checkPV(pvPEF, tt.transformPVCoordinates(pvTEME), 2.2, 1.2e-3);
        
        TEMEFrame TEMEFrame = new TEMEFrame(false, AbsoluteDate.J2000_EPOCH, "TEME wo corr");
        PEFFrame  PEFFrame  = new PEFFrame(false, AbsoluteDate.J2000_EPOCH, "PEF wo corr");
        
        Transform tf = TEMEFrame.getTransformTo(PEFFrame, t0);
        checkPV(pvPEF, tf.transformPVCoordinates(pvTEME), 0.3, 1.6e-4);

    }

    public void testAASReferenceGEO() throws OrekitException {

        // this reference test has been extracted from the following paper:
        // Implementation Issues Surrounding the New IAU Reference Systems for Astrodynamics
        // David A. Vallado, John H. Seago, P. Kenneth Seidelmann
        // http://www.centerforspace.com/downloads/files/pubs/AAS-06-134.pdf
        AbsoluteDate t0 = new AbsoluteDate(new DateComponents(2004, 06, 01),
                                           TimeComponents.H00,
                                           UTCScale.getInstance());

        Transform tt = Frame.getTEME(true).getTransformTo(Frame.getPEF(true), t0);
        Transform tf = Frame.getTEME(false).getTransformTo(Frame.getPEF(false), t0);

// TOD iau76
        PVCoordinates pvTEME =
            new PVCoordinates(new Vector3D(-40577427.7501, -11500096.1306, 10293.2583),
                              new Vector3D(837.552338, -2957.524176, -0.928772));

//PEF iau76
        PVCoordinates pvPEF =
            new PVCoordinates(new Vector3D(24796919.2956, -34115870.9001, 10293.2583),
                              new Vector3D(-0.979178, -1.476540, -0.928772));

        checkPV(pvPEF, tt.transformPVCoordinates(pvTEME), 10.3, 1.5e-5);

        checkPV(pvPEF, tf.transformPVCoordinates(pvTEME), 0.2, 1.5e-5);

    }

    public void setUp() {
        String root = getClass().getClassLoader().getResource("compressed-data").getPath();
        System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, root);
    }

    private void checkPV(PVCoordinates reference,
                         PVCoordinates result, double positionThreshold,
                         double velocityThreshold) {

        Vector3D dP = result.getPosition().subtract(reference.getPosition());
        Vector3D dV = result.getVelocity().subtract(reference.getVelocity());
        assertEquals(0, dP.getNorm(), positionThreshold);
        assertEquals(0, dV.getNorm(), velocityThreshold);
    }

    public static Test suite() {
        return new TestSuite(PEFFrameTest.class);
    }

}
