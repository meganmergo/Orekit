/* Copyright 2002-2019 CS Systèmes d'Information
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
package org.orekit.estimation.measurements.gnss;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.hipparchus.linear.DiagonalMatrix;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well19937a;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathArrays;
import org.hipparchus.util.MathArrays.Position;
import org.hipparchus.util.Precision;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractLambdaReducerTest {

    protected abstract AbstractLambdaReducer buildReducer(double[] floatAmbiguitiess, int[] indirection, RealMatrix covariance);

    @Test
    public void testSimpleFullDecomposition() {
        final RealMatrix refLow = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 0.0, 0.0, 0.0 },
            { 2.0, 1.0, 0.0, 0.0 },
            { 3.0, 4.0, 1.0, 0.0 },
            { 5.0, 6.0, 7.0, 1.0 }
        });
        final RealMatrix refDiag = MatrixUtils.createRealDiagonalMatrix(new double[] {
            5.0, 7.0, 9.0, 11.0
        });
        final RealMatrix covariance = refLow.transposeMultiply(refDiag).multiply(refLow);
        final int[] indirection = new int[] { 0, 1, 2, 3 };
        AbstractLambdaReducer reducer = buildReducer(new double[indirection.length], indirection, covariance);
        reducer.ltdlDecomposition();
        Assert.assertEquals(0.0, refLow.subtract(getLow(reducer)).getNorm(), 1.0e-15);
        Assert.assertEquals(0.0, refDiag.subtract(getDiag(reducer)).getNorm(), 1.0e-15);
    }

    @Test
    public void testRandomDecomposition() {

        RandomGenerator random = new Well19937a(0x7aa94f3683fd08c1l);
        for (int k = 0; k < 1000; ++k) {
            // generate random test data
            final int        n           = FastMath.max(2, 1 + random.nextInt(20));
            final RealMatrix covariance  = createRandomSymmetricMatrix(n, random);
            final int[]      indirection = createRandomIndirectionArray(n, random);

            // perform decomposition test
            doTestDecomposition(indirection, covariance);

        }

    }

    @Test
    public void testIntegerGaussTransformation() {

        RandomGenerator random = new Well19937a(0x08e9e32dcd0f9dbdl);
        for (int k = 0; k < 1000; ++k) {
            // generate random test data
            final int        n           = FastMath.max(2, 1 + random.nextInt(20));
   
            final RealMatrix covariance  = createRandomSymmetricMatrix(n, random);
            final int[]      indirection = createRandomIndirectionArray(n, random);

            // perform integer Gauss transformation test
            doTestIntegerGaussTransformation(random, indirection, covariance);

        }
    }

    @Test
    public void testPermutation() {

        RandomGenerator random = new Well19937a(0xf824c33093974ee5l);
        for (int k = 0; k < 1000; ++k) {
            // generate random test data
            final int        n           = FastMath.max(2, 1 + random.nextInt(20));
   
            final RealMatrix covariance  = createRandomSymmetricMatrix(n, random);
            final int[]      indirection = createRandomIndirectionArray(n, random);

            // perform permutation transformation test
            doTestPermutation(random, indirection, covariance);

        }
    }

    @Test
    public void testJoostenTiberiusFAQ() {
        // this test corresponds to the "LAMBDA: FAQs" paper by Peter Joosten and Christian Tiberius

        final double[] floatAmbiguities = new double[] {
            5.450, 3.100, 2.970
        };
        final int[] indirection = new int[] { 0, 1, 2 };
        final RealMatrix covariance = MatrixUtils.createRealMatrix(new double[][] {
            { 6.290, 5.978, 0.544 },
            { 5.978, 6.292, 2.340 },
            { 0.544, 2.340, 6.288 }
        });

        final AbstractLambdaReducer reducer = buildReducer(floatAmbiguities, indirection, covariance);
        reducer.ltdlDecomposition();
        reducer.reduction();

        final RealMatrix zRef = MatrixUtils.createRealMatrix(new double[][] {
            { -2,  3,  1 },
            {  3, -3, -1 },
            { -1,  1,  0 }
        });

        Assert.assertEquals(0.0,
                            zRef.subtract(getZTranformation(reducer)).getNorm(),
                            1.0e-15);

        // TODO implement and test search method

    }

    private void doTestDecomposition(final int[] indirection, final RealMatrix covariance) {
        final AbstractLambdaReducer reducer = buildReducer(new double[indirection.length], indirection, covariance);
        reducer.ltdlDecomposition();
        final RealMatrix extracted = MatrixUtils.createRealMatrix(indirection.length, indirection.length);
        for (int i = 0; i < indirection.length; ++i) {
            for (int j = 0; j < indirection.length; ++j) {
                extracted.setEntry(i, j, covariance.getEntry(indirection[i], indirection[j]));
            }
        }
        final RealMatrix rebuilt = getLow(reducer).
                                   transposeMultiply(getDiag(reducer)).
                                   multiply(getLow(reducer));
        double maxError = 0;
        for (int i = 0; i < indirection.length; ++i) {
            for (int j = 0; j < indirection.length; ++j) {
                maxError = FastMath.max(maxError,
                                        FastMath.abs(covariance.getEntry(indirection[i], indirection[j]) -
                                                     rebuilt.getEntry(i, j)));
            }
        }
        Assert.assertEquals(0.0, maxError, 2.0e-11);

    }

    private void doTestIntegerGaussTransformation(final RandomGenerator random,
                                                  final int[] indirection, final RealMatrix covariance) {
        final int n = indirection.length;
        final double[] floatAmbiguities = new double[n];
        for (int i = 0; i < n; ++i) {
            floatAmbiguities[i] = 2 * random.nextDouble() - 1.0;
        }
        final AbstractLambdaReducer reducer = buildReducer(floatAmbiguities, indirection, covariance);
        reducer.ltdlDecomposition();
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(n);
        RealMatrix zRef     = identity;
        RealMatrix lowRef   = getLow(reducer);
        RealMatrix diagRef  = getDiag(reducer);
        double[]   aBase    = getDecorrelated(reducer).clone();
        double[]   aRef     = aBase;
        Assert.assertEquals(0.0,
                            zRef.subtract(getZTranformation(reducer)).getNorm(),
                            1.0e-15);
        for (int k = 0; k < 10; ++k) {
            final ReferenceIntegerGaussTransformation rigt = createRandomIntegerGaussTransformation(getLow(reducer), random);
            reducer.integerGaussTransformation(rigt.i, rigt.j);

            // check cumulated Z transform, with reference based on naive matrix multiplication
            zRef = zRef.multiply(rigt.z);
            Assert.assertEquals(0.0,
                                zRef.subtract(getZTranformation(reducer)).getNorm(),
                                Precision.SAFE_MIN);

            // check Z and Z⁻¹
            Assert.assertEquals(0.0,
                                identity.subtract(getZTranformation(reducer).multiply(getZInverseTranformation(reducer))).getNorm(),
                                Precision.SAFE_MIN);

            // check diagonal part, which should not change
            Assert.assertEquals(0.0,
                                diagRef.subtract(getDiag(reducer)).getNorm(),
                                Precision.SAFE_MIN);

            // check cumulated low triangular part, with reference based on naive matrix multiplication
            lowRef = lowRef.multiply(rigt.z);
            Assert.assertEquals(0.0,
                                lowRef.subtract(getLow(reducer)).getNorm(),
                                Precision.SAFE_MIN);
            Assert.assertTrue(getLow(reducer).getEntry(rigt.i, rigt.j) <= 0.5);

            // check ambiguities, with reference based on single step naive matrix multiplication
            aRef = rigt.z.transpose().operate(aRef);
            for (int i = 0; i < aRef.length; ++i) {
                Assert.assertEquals(aRef[i], getDecorrelated(reducer)[i], 4.0e-14);
            }

            // check ambiguities, with reference based on cumulated naive matrix multiplication
            final double[] aRef2 = zRef.transpose().operate(aBase);
            for (int i = 0; i < aRef2.length; ++i) {
                Assert.assertEquals(aRef2[i], getDecorrelated(reducer)[i], 4.0e-14);
            }

        }
    }

    private void doTestPermutation(final RandomGenerator random,
                                   final int[] indirection, final RealMatrix covariance) {
        final double[] floatAmbiguities = new double[indirection.length];
        for (int i = 0; i < floatAmbiguities.length; ++i) {
            floatAmbiguities[i] = 2 * random.nextDouble() - 1.0;
        }
        final AbstractLambdaReducer reducer = buildReducer(floatAmbiguities, indirection, covariance);
        reducer.ltdlDecomposition();
        RealMatrix filteredCovariance = filterCovariance(covariance, indirection);
        RealMatrix zRef    = MatrixUtils.createRealIdentityMatrix(indirection.length);
        double[] aBase = getDecorrelated(reducer).clone();
        double[] aRef  = aBase.clone();
        Assert.assertEquals(0.0,
                            zRef.subtract(getZTranformation(reducer)).getNorm(),
                            1.0e-15);
        for (int k = 0; k < 10; ++k) {
            final ReferencePermutation rp = createRandomPermutation(getLow(reducer),
                                                                    getDiag(reducer),
                                                                    random);
            reducer.permutation(rp.i, rp.delta);

            // check cumulated Z transform, with reference based on naive matrix multiplication
            zRef = zRef.multiply(rp.z);
            Assert.assertEquals(0.0,
                                zRef.subtract(getZTranformation(reducer)).getNorm(),
                                Precision.SAFE_MIN);

            // check rebuilt permuted covariance
            RealMatrix rebuilt = getLow(reducer).
                                 transposeMultiply(getDiag(reducer)).
                                 multiply(getLow(reducer));
            RealMatrix permutedCovariance = zRef.
                                            transposeMultiply(filteredCovariance).
                                            multiply(zRef);
            Assert.assertEquals(0.0,
                                permutedCovariance.subtract(rebuilt).getNorm(),
                                3.0e-10);

            // check ambiguities, with reference based on direct permutation
            final double tmp = aRef[rp.i];
            aRef[rp.i]     = aRef[rp.i + 1];
            aRef[rp.i + 1] = tmp;
            for (int i = 0; i < aRef.length; ++i) {
                Assert.assertEquals(aRef[i], getDecorrelated(reducer)[i], 4.0e-14);
            }

            // check ambiguities, with reference based on cumulated naive matrix multiplication
            final double[] aRef2 = zRef.transpose().operate(aBase);
            for (int i = 0; i < aRef2.length; ++i) {
                Assert.assertEquals(aRef2[i], getDecorrelated(reducer)[i], 4.0e-14);
            }

        }
    }

    private int getN(final AbstractLambdaReducer reducer) {
        try {
            final Field nField = AbstractLambdaReducer.class.getDeclaredField("n");
            nField.setAccessible(true);
            return ((Integer) nField.get(reducer)).intValue();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assert.fail(e.getLocalizedMessage());
            return -1;
        }
    }

    private RealMatrix getLow(final AbstractLambdaReducer reducer) {
        try {
            final int n = getN(reducer);
            final Field lowField = AbstractLambdaReducer.class.getDeclaredField("low");
            lowField.setAccessible(true);
            final double[] low = (double[]) lowField.get(reducer);
            final RealMatrix lowM = MatrixUtils.createRealMatrix(n, n);
            int k = 0;
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < i; ++j) {
                    lowM.setEntry(i, j, low[k++]);
                }
                lowM.setEntry(i, i, 1.0);
            }
            return lowM;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assert.fail(e.getLocalizedMessage());
            return null;
        }
    }

    public DiagonalMatrix getDiag(final AbstractLambdaReducer reducer) {
        try {
            final Field diagField = AbstractLambdaReducer.class.getDeclaredField("diag");
            diagField.setAccessible(true);
            final double[] diag = (double[]) diagField.get(reducer);
            return MatrixUtils.createRealDiagonalMatrix(diag);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assert.fail(e.getLocalizedMessage());
            return null;
        }
    }

    public RealMatrix getZTranformation(final AbstractLambdaReducer reducer) {
        return dogetZs(reducer, "zTransformation");
    }

    public RealMatrix getZInverseTranformation(final AbstractLambdaReducer reducer) {
        return dogetZs(reducer, "zInverseTransformation");
    }

    private RealMatrix dogetZs(final AbstractLambdaReducer reducer, final String fieldName) {
        try {
            final int n = getN(reducer);
            final Field zField = AbstractLambdaReducer.class.getDeclaredField(fieldName);
            zField.setAccessible(true);
            final int[] z = (int[]) zField.get(reducer);
            final RealMatrix zM = MatrixUtils.createRealMatrix(n, n);
            int k = 0;
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    zM.setEntry(i, j, z[k++]);
                }
            }
            return zM;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assert.fail(e.getLocalizedMessage());
            return null;
        }
    }

    public double[] getDecorrelated(final AbstractLambdaReducer reducer) {
        try {
            final Field decorrelatedField = AbstractLambdaReducer.class.getDeclaredField("decorrelated");
            decorrelatedField.setAccessible(true);
            return (double[]) decorrelatedField.get(reducer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assert.fail(e.getLocalizedMessage());
            return null;
        }
    }

    private RealMatrix createRandomSymmetricMatrix(final int n, final RandomGenerator random) {
        final RealMatrix matrix = MatrixUtils.createRealMatrix(n, n);
        for (int i = 0; i < n; ++i) {                
            for (int j = 0; j <= i; ++j) {
                final double entry = 20 * random.nextDouble() - 10;
                matrix.setEntry(i, j, entry);
                matrix.setEntry(j, i, entry);
            }
        }
        return matrix;
    }

    private int[] createRandomIndirectionArray(final int n, final RandomGenerator random) {
        final int[] all = new int[n];
        for (int i = 0; i < all.length; ++i) {
            all[i] = i;
        }
        MathArrays.shuffle(all, 0, Position.TAIL, random);
        return Arrays.copyOf(all, FastMath.max(2, 1 + random.nextInt(n)));
    }

    private ReferenceIntegerGaussTransformation createRandomIntegerGaussTransformation(final RealMatrix low,
                                                                                       final RandomGenerator random) {
        final int n = low.getRowDimension();
        int i = random.nextInt(n);
        int j = i;
        while (j == i) {
            j = random.nextInt(n);
        }
        if (i < j) {
            final int tmp = i;
            i = j;
            j = tmp;
        }
        return new ReferenceIntegerGaussTransformation(n, i, j, (int) FastMath.rint(low.getEntry(i, j))); 
    }

    private static class ReferenceIntegerGaussTransformation {
        private final int i;
        private final int j;
        private final RealMatrix z;
        ReferenceIntegerGaussTransformation(int n, int i, int j, int mu) {
            this.i = i;
            this.j = j;
            this.z = MatrixUtils.createRealIdentityMatrix(n);
            z.setEntry(i, j, -mu);
        }
    }

    private ReferencePermutation createRandomPermutation(final RealMatrix low,
                                                         final RealMatrix diag,
                                                         final RandomGenerator random) {
        final int    n     = low.getRowDimension();
        final int    i     = random.nextInt(n - 1);
        final double dk0   = diag.getEntry(i, i);
        final double dk1   = diag.getEntry(i + 1, i + 1);
        final double lk1k0 = low.getEntry(i + 1, i);
        return new ReferencePermutation(n, i, dk0 + lk1k0 * lk1k0 * dk1); 
    }

    private static class ReferencePermutation {
        private final int i;
        private double delta;
        private final RealMatrix z;
        ReferencePermutation(int n, int i, double delta) {
            this.i     = i;
            this.delta = delta;
            this.z     = MatrixUtils.createRealIdentityMatrix(n);
            z.setEntry(i,     i,     0);
            z.setEntry(i,     i + 1, 1);
            z.setEntry(i + 1, i,     1);
            z.setEntry(i + 1, i + 1, 0);
        }
    }

    RealMatrix filterCovariance(final RealMatrix covariance, int[] indirection) {
        RealMatrix filtered = MatrixUtils.createRealMatrix(indirection.length, indirection.length);
        for (int i = 0; i < indirection.length; ++i) {
            for (int j = 0; j <= i; ++j) {
                filtered.setEntry(i, j, covariance.getEntry(indirection[i], indirection[j]));
                filtered.setEntry(j, i, covariance.getEntry(indirection[i], indirection[j]));
            }
        }
        return filtered;
    }

}

