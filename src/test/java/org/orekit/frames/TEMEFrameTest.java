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

import java.io.FileNotFoundException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.math.geometry.Vector3D;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.TAIScale;
import org.orekit.time.TimeComponents;
import org.orekit.time.UTCScale;
import org.orekit.utils.PVCoordinates;


public class TEMEFrameTest extends TestCase {

    public void testAASReferenceLEO() throws OrekitException {

        // this reference test has been extracted from the following paper:
        // Implementation Issues Surrounding the New IAU Reference Systems for Astrodynamics
        // David A. Vallado, John H. Seago, P. Kenneth Seidelmann
        // http://www.centerforspace.com/downloads/files/pubs/AAS-06-134.pdf
        AbsoluteDate t0 = new AbsoluteDate(new DateComponents(2004, 04, 06),
                                           new TimeComponents(07, 51, 28.386009),
                                           UTCScale.getInstance());

        Transform tt = Frame.getMEME(true).getTransformTo(Frame.getTEME(true), t0);
        Transform tf = Frame.getMEME(false).getTransformTo(Frame.getTEME(false), t0);

//TOD iau76
        PVCoordinates pvTODiau76 =
            new PVCoordinates(new Vector3D(5094514.7804, 6127366.4612, 6380344.5328),
                              new Vector3D(-4746.088567, 786.077222, 5531.931288));
//MOD iau76
        PVCoordinates pvMODiau76 =
            new PVCoordinates(new Vector3D(5094029.0167, 6127870.9363, 6380247.8885),
                              new Vector3D(-4746.262495, 786.014149, 5531.791025));
//MOD iau76 w corr
        PVCoordinates pvMODiau76Wcorr =
            new PVCoordinates(new Vector3D(5094028.3745, 6127870.8164, 6380248.5164),
                              new Vector3D(-4746.263052, 786.014045, 5531.790562));
 
        checkPV(pvTODiau76, tt.transformPVCoordinates(pvMODiau76Wcorr), 1.9, 1.2e-3);
        checkPV(pvTODiau76, tt.transformPVCoordinates(pvMODiau76), 2.5, 1.5e-3);

        checkPV(pvTODiau76, tf.transformPVCoordinates(pvMODiau76), 6.8e-5, 6.0e-7);
        checkPV(pvTODiau76, tf.transformPVCoordinates(pvMODiau76Wcorr), 0.90615, 7.4e-4);

//        final Transform transform = Frame.getMEME(true).getTransformTo(Frame.getMEME(false), t0);
//        final double error = transform.getRotation().getAngle() * 648000 / Math.PI;
//        System.out.println("\nError : " + error + "\"");

    }

    public void testAASReferenceGEO() throws OrekitException {

//        J2000 = 2451545.5
//        eps0  = iauObl80(2454388.5);
//        vvd(eps0, 0.4090751347643816218, 1e-14, "iauObl80", "", status);
//            moe = 0.40907513475969354
//        AbsoluteDate t0 = new AbsoluteDate(new DateComponents(2844),
//                                           TimeComponents.H00,
//                                           UTCScale.getInstance());
        
//        iauNut80(2453736.5, 0.0, &dpsi, &deps);
//        vvd(dpsi, -0.9643658353226563966e-5, 1e-13, "iauNut80", "dpsi", status);
//           dpsi = -0.964296426822596E-5
//        vvd(deps,  0.4060051006879713322e-4, 1e-13, "iauNut80", "deps", status);
//            deps = 0.4060070455616641E-4
//        AbsoluteDate t0 = new AbsoluteDate(new DateComponents(2192),
//                                           TimeComponents.H00,
//                                           UTCScale.getInstance());

        // this reference test has been extracted from the following paper:
        // Implementation Issues Surrounding the New IAU Reference Systems for Astrodynamics
        // David A. Vallado, John H. Seago, P. Kenneth Seidelmann
        // http://www.centerforspace.com/downloads/files/pubs/AAS-06-134.pdf

        AbsoluteDate t0 = new AbsoluteDate(new DateComponents(2004, 06, 01),
                                           TimeComponents.H00,
                                           UTCScale.getInstance());

        Transform tt = Frame.getMEME(true).getTransformTo(Frame.getTEME(true), t0);
        Transform tf = Frame.getMEME(false).getTransformTo(Frame.getTEME(false), t0);

//TOD iau76
        PVCoordinates pvTODiau76 =
            new PVCoordinates(new Vector3D(-40577427.7501, -11500096.1306, 10293.2583),
                              new Vector3D(837.552338, -2957.524176, -0.928772));
//MOD iau76
        PVCoordinates pvMODiau76 =
            new PVCoordinates(new Vector3D(-40576822.6385, -11502231.5013, 9738.2304),
                              new Vector3D(837.708020, -2957.480118, -0.814275));
//MOD iau76 w corr
        PVCoordinates pvMODiau76Wcorr =
            new PVCoordinates(new Vector3D(-40576822.6395, -11502231.5015, 9733.7842),
                              new Vector3D(837.708020, -2957.480117, -0.814253));

        checkPV(pvTODiau76, tt.transformPVCoordinates(pvMODiau76Wcorr), 10.1, 7.4e-4);
        checkPV(pvTODiau76, tf.transformPVCoordinates(pvMODiau76Wcorr), 4.5, 2.3e-5);

        checkPV(pvTODiau76, tf.transformPVCoordinates(pvMODiau76), 4.6e-5, 8.8e-7);
        checkPV(pvTODiau76, tt.transformPVCoordinates(pvMODiau76), 11.1, 7.4e-4);

    }

    public void testInterpolationAccuracy() throws OrekitException, FileNotFoundException {

        final boolean withNutationCorrection = true;
        
        TEMEFrame interpolatingFrame =
            new TEMEFrame(withNutationCorrection, AbsoluteDate.J2000_EPOCH, "");
        NonInterpolatingTEMEFrame nonInterpolatingFrame =
            new NonInterpolatingTEMEFrame(withNutationCorrection, AbsoluteDate.J2000_EPOCH, "");

        // the following time range is located around the maximal observed error
        AbsoluteDate start = new AbsoluteDate(2002, 11, 11, 0, 0, 0.0, TAIScale.getInstance());
        AbsoluteDate end   = new AbsoluteDate(2002, 11, 15, 6, 0, 0.0, TAIScale.getInstance());
        double maxError = 0.0;
//        System.out.println("        TAI date          Error (arcseconds)");
        for (AbsoluteDate date = start;
             date.compareTo(end) < 0;
             date = new AbsoluteDate(date, 60)) {
            final Transform transform =
                interpolatingFrame.getTransformTo(nonInterpolatingFrame, date);
            final double error = transform.getRotation().getAngle() * 648000 / Math.PI;
            maxError = Math.max(maxError, error);
//            System.out.println(date.toString(TAIScale.getInstance()) + "   " + error);
        }
//        System.out.println("\nError max : " + maxError);

        assertTrue(maxError < 7.2e-11);

    }

    private class NonInterpolatingTEMEFrame extends TEMEFrame {
        private static final long serialVersionUID = -7116622345154042273L;
        public NonInterpolatingTEMEFrame(final boolean ignoreNutationCorrection,
                                         final AbsoluteDate date, final String name)
            throws OrekitException {
            super(ignoreNutationCorrection, date, name);
        }
        protected void setInterpolatedNutationElements(final double t) {
            computeNutationElements(t);
        }
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
        return new TestSuite(TEMEFrameTest.class);
    }

}
