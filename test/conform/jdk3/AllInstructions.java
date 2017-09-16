/***
 * ASM tests
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package jdk3;

/**
 * Class which, compiled with the JDK 1.3.0, produces all the JVM instructions
 * from that version, in particular jsr and ret (except nop and swap). Must be
 * compiled with "javac -g".
 */
class AllInstructions {
    private int f;
    private long g;
    private AllInstructions field;
    private static AllInstructions staticField;

    AllInstructions() {
    }

    AllInstructions(int v0, float v1, long v2, double v3, Object v4) {
    }

    public static int intInstructions(int v0, int v1, int v2, int v3, int v4,
            int v5, int v6, int v7, int v8) {
        boolean b0 = v0 < -1;
        boolean b1 = v1 > 1;
        boolean b2 = v2 <= 2;
        boolean b3 = v3 >= 3;
        boolean b4 = v4 == 4;
        boolean b5 = v5 != 5;
        v0 = b0 ? (v6 + 5) : (v6 - 5);
        v1 = b1 ? (v7 * 100) : (v7 / 100);
        v2 = b2 ? (v8 % 10000) : (~v8);
        v3 = b3 ? (v0 & 1000000) : (v0 | 1000000);
        v4 = b4 ? (v1 ^ v2) : (v1 << v2);
        v5 = b5 ? (v2 >> v3) : (v2 >>> v3);
        v6 += 1;
        v7 = v6 < 0 ? -v6 : v6;
        v1 = v0 < 0 ? v1 : v2;
        v2 = v1 > 0 ? v2 : v3;
        v3 = v2 <= 0 ? v3 : v4;
        v4 = v3 >= 0 ? v4 : v5;
        v5 = v4 == 0 ? v5 : v6;
        v6 = v5 != 0 ? v6 : v7;
        return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
    }

    public static long longInstructions(long v0, long v1, long v2, long v3,
            long v4, long v5, long v6, long v7, long v8) {
        boolean b0 = v0 < -1L;
        boolean b1 = v1 > 1L;
        boolean b2 = v2 <= 2L;
        boolean b3 = v3 >= 3L;
        boolean b4 = v4 == 4L;
        boolean b5 = v5 != 5L;
        v0 = b0 ? (v6 + 5L) : (v6 - 5L);
        v1 = b1 ? (v7 * 100L) : (v7 / 100L);
        v2 = b2 ? (v8 % 10000L) : (~v8);
        v3 = b3 ? (v0 & 1000000L) : (v0 | 1000000L);
        v4 = b4 ? (v1 ^ v2) : (v1 << v2);
        v5 = b5 ? (v2 >> v3) : (v2 >>> v3);
        v6 += 1L;
        v7 = v6 < 0L ? -v6 : v6;
        return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
    }

    public static float floatInstructions(float v0, float v1, float v2,
            float v3, float v4, float v5, float v6, float v7, float v8) {
        boolean b0 = v0 < -1f;
        boolean b1 = v1 > 1f;
        boolean b2 = v2 <= 2f;
        boolean b3 = v3 >= 3f;
        boolean b4 = v4 == 4f;
        boolean b5 = v5 != 5f;
        v0 = b0 ? (v6 + 5f) : (v6 - 5f);
        v1 = b1 ? (v7 * 100f) : (v7 / 100f);
        v2 = b2 ? (v8 % 10000f) : v8;
        v3 = b3 ? -v3 : v3;
        v4 = b4 ? -v4 : v4;
        v5 = b5 ? -v5 : v5;
        v6 += 1f;
        v7 = v6 < 0f ? -v6 : v6;
        v8 = v7;
        return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
    }

    public static double doubleInstructions(double v0, double v1, double v2,
            double v3, double v4, double v5, double v6, double v7, double v8) {
        boolean b0 = v0 < -1d;
        boolean b1 = v1 > 1d;
        boolean b2 = v2 <= 2d;
        boolean b3 = v3 >= 3d;
        boolean b4 = v4 == 4d;
        boolean b5 = v5 != 5d;
        v0 = b0 ? (v6 + 5d) : (v6 - 5d);
        v1 = b1 ? (v7 * 100d) : (v7 / 100d);
        v2 = b2 ? (v8 % 10000d) : v8;
        v3 = b3 ? -v3 : v3;
        v4 = b4 ? -v4 : v4;
        v5 = b5 ? -v5 : v5;
        v6 += 1d;
        v7 = v6 < 0d ? -v6 : v6;
        return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
    }

    public static double castInstructions(int v0, long v1, long v2) {
        byte v3 = (byte) v0;
        char v4 = (char) v1;
        short v5 = (short) v2;
        long v6 = (long) v3;
        float v7 = (float) v4;
        double v8 = (double) v5;
        v1 = v6;
        v2 = v1;
        v6 = (long) v8;
        return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
    }

    public static float castInstructions(float v0, double v1, double v2) {
        byte v3 = (byte) v0;
        char v4 = (char) v1;
        short v5 = (short) v2;
        long v6 = (long) v3;
        float v7 = (float) v4;
        double v8 = (double) v5;
        v1 = v6;
        v2 = v1;
        v6 = (long) v7;
        return (float) (v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8);
    }

    public static Object objectInstructions(Object v0, Object v1, Object v2,
            Object v3, Object v4) {
        boolean b0 = v0 == v1;
        boolean b1 = v1 != v2;
        boolean b2 = v2 == null;
        boolean b3 = v3 != null;
        boolean b4 = v4 instanceof String;
        v0 = b0 ? null : v0;
        v1 = b1 ? v1 : v0;
        v2 = b2 ? v2 : v1;
        v3 = b3 ? v3 : v2;
        v4 = b4 ? new Integer(((String) v4).length()) : v3;
        return v4;
    }

    public static Object[] arrayInstructions(byte[] v0, char[] v1, short[] v2,
            int[] v3, long[] v4, float[] v5, double[] v6, Object[] v7) {
        v0[1] = v0[0];
        v1[1] = v1[0];
        v2[1] = v2[0];
        v3[1] = v3[0];
        v4[1] = v4[0];
        v5[1] = v5[0];
        v6[1] = v6[0];
        v7[1] = v7[0];
        Object[] v8 = new Object[v7.length];
        v8[0] = new int[4][8][16];
        return v8;
    }

    public void fieldInstructions() {
        AllInstructions c = field;
        field = staticField;
        staticField = c;
    }

    public void methodInstructions(Runnable v0) {
        AllInstructions c = new AllInstructions();
        c.fieldInstructions();
        monitorInstructions(c);
        v0.run();
    }

    public static int lookupSwitchInstruction(int v0) {
        switch (v0) {
        case 1000:
            return 1;
        case 10000:
            return 2;
        case 100000:
            return 3;
        default:
            return -1;
        }
    }

    public static int tableSwitchInstruction(int v0) {
        switch (v0) {
        case 0:
            return 1;
        case 1:
            return 2;
        case 2:
            return 3;
        default:
            return -1;
        }
    }

    public static String monitorInstructions(Object v0) {
        synchronized (v0) {
            return v0.toString();
        }
    }

    public int dupX1Instruction() {
        return f++;
    }

    public long dup2X1Instruction() {
        return g++;
    }

    public void dupX2Instruction(int[] v0, int[] v1) {
        v0[0] = v1[0] = 0;
    }

    public void dup2X2Instruction(long[] v0, long[] v1) {
        v0[0] = v1[0] = 0;
    }

    public void popInstructions() {
        dupX1Instruction();
        dup2X1Instruction();
    }

    public int jsrAndRetInstructions(int v0) throws Exception {
        int u0 = v0 + 1;
        try {
            u0 = jsrAndRetInstructions(u0);
        } catch (Throwable t) {
            return -1;
        } finally {
            u0++;
        }
        return u0;
    }

    public AllInstructions wideInstructions(int v0, long v1, long v2, long v3,
            long v4, long v5, long v6, long v7, long v8, long v9, long v10,
            long v11, long v12, long v13, long v14, long v15, long v16,
            long v17, long v18, long v19, long v20, long v21, long v22,
            long v23, long v24, long v25, long v26, long v27, long v28,
            long v29, long v30, long v31, long v32, long v33, long v34,
            long v35, long v36, long v37, long v38, long v39, long v40,
            long v41, long v42, long v43, long v44, long v45, long v46,
            long v47, long v48, long v49, long v50, long v51, long v52,
            long v53, long v54, long v55, long v56, long v57, long v58,
            long v59, long v60, long v61, long v62, long v63, long v64,
            long v65, long v66, long v67, long v68, long v69, long v70,
            long v71, long v72, long v73, long v74, long v75, long v76,
            long v77, long v78, long v79, long v80, long v81, long v82,
            long v83, long v84, long v85, long v86, long v87, long v88,
            long v89, long v90, long v91, long v92, long v93, long v94,
            long v95, long v96, long v97, long v98, long v99, long v100,
            long v101, long v102, long v103, long v104, long v105, long v106,
            long v107, long v108, long v109, long v110, long v111, long v112,
            long v113, long v114, long v115, long v116, long v117, long v118,
            long v119, long v120, long v121, long v122, long v123, int v124,
            float v125, long v126, double v127, Object v128) {
        int[] u0 = { 70001, 70002, 70003, 70004, 70005, 70006, 70007, 70008,
                70009, 70010, 70011, 70012, 70013, 70014, 70015, 70016, 70017,
                70018, 70019, 70020, 70021, 70022, 70023, 70024, 70025, 70026,
                70027, 70028, 70029, 70030, 70031, 70032, 70033, 70034, 70035,
                70036, 70037, 70038, 70039, 70040, 70041, 70042, 70043, 70044,
                70045, 70046, 70047, 70048, 70049, 70050, 70051, 70052, 70053,
                70054, 70055, 70056, 70057, 70058, 70059, 70060, 70061, 70062,
                70063, 70064, 70065, 70066, 70067, 70068, 70069, 70070, 70071,
                70072, 70073, 70074, 70075, 70076, 70077, 70078, 70079, 70080,
                70081, 70082, 70083, 70084, 70085, 70086, 70087, 70088, 70089,
                70090, 70091, 70092, 70093, 70094, 70095, 70096, 70097, 70098,
                70099, 70100, 70101, 70102, 70103, 70104, 70105, 70106, 70107,
                70108, 70109, 70110, 70111, 70112, 70113, 70114, 70115, 70116,
                70117, 70118, 70119, 70120, 70121, 70122, 70123, 70124, 70125,
                70126, 70127, 70128, 70129, 70130, 70131, 70132, 70133, 70134,
                70135, 70136, 70137, 70138, 70139, 70140, 70141, 70142, 70143,
                70144, 70145, 70146, 70147, 70148, 70149, 70150, 70151, 70152,
                70153, 70154, 70155, 70156, 70157, 70158, 70159, 70160, 70161,
                70162, 70163, 70164, 70165, 70166, 70167, 70168, 70169, 70170,
                70171, 70172, 70173, 70174, 70175, 70176, 70177, 70178, 70179,
                70180, 70181, 70182, 70183, 70184, 70185, 70186, 70187, 70188,
                70189, 70190, 70191, 70192, 70193, 70194, 70195, 70196, 70197,
                70198, 70199, 70200, 70201, 70202, 70203, 70204, 70205, 70206,
                70207, 70208, 70209, 70210, 70211, 70212, 70213, 70214, 70215,
                70216, 70217, 70218, 70219, 70220, 70221, 70222, 70223, 70224,
                70225, 70226, 70227, 70228, 70229, 70230, 70231, 70232, 70233,
                70234, 70235, 70236, 70237, 70238, 70239, 70240, 70241, 70242,
                70243, 70244, 70245, 70246, 70247, 70248, 70249, 70250, 70251,
                70252, 70253, 70254, 70255 };
        int u124 = v124 < 0 ? -v124 : v124;
        float u125 = v125 < 0f ? -v125 : v125;
        long u126 = v126 < 0L ? -v126 : v126;
        double u127 = v127 < 0d ? -v127 : v127;
        String u128 = v128 == null ? null : v128.toString();
        try {
            for (int i = 0; i < v0; ++i) {
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
            }
        } catch (Throwable t) {
            return null;
        } finally {
            u0 = null;
        }
        return new AllInstructions(u124 + u0[f % u0.length], u125, u126, u127,
                u128);
    }
}
