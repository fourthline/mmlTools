/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mmlTools.optimizer;

import fourthline.mmlTools.core.MMLTokenizer;


/**
 * "<b?>" -> "c-?"
 * ">c?<" -> "b+?"
 */
public final class BpCmOptimizer implements MMLStringOptimizer.Optimizer {

	private enum BmCpState {
		NONE {
			/**
			 * "<" -> B1
			 * ">" -> C1
			 */
			@Override
			public BmCpState nextStatus(String token) {
				if (token.equals("<")) {
					return B1;
				} else if (token.equals(">")) {
					return C1;
				}
				return NONE;
			}
		},
		B1 {
			/**
			 * "<b?" -> B2
			 */
			@Override
			public BmCpState nextStatus(String token) {
				if (token.equals("<")) {
					return B1;
				} else if (MMLTokenizer.noteNames(token)[0].toLowerCase().equals("b")) {
					return B2;
				}
				return NONE;
			}
		},
		B2 {
			/**
			 * "<b?>" -> B3
			 */
			@Override
			public BmCpState nextStatus(String token) {
				return (token.equals(">")) ? B3 : NONE;
			}
		},
		B3 {
			/**
			 * "<b?>" -> "c-?"
			 */
			@Override
			public String optimize(String s) {
				return s.replaceAll("<b", "c-")
						.replaceAll("<B", "C-")
						.replace(">", "");
			}
		},
		C1 {
			/**
			 * ">c?" -> C2
			 */
			@Override
			public BmCpState nextStatus(String token) {
				if (token.equals(">")) {
					return C1;
				} else if (MMLTokenizer.noteNames(token)[0].toLowerCase().equals("c")) {
					return C2;
				}
				return NONE;
			}
		},
		C2 {
			/**
			 * ">c?<" -> C3
			 */
			@Override
			public BmCpState nextStatus(String token) {
				return (token.equals("<")) ? C3 : NONE;
			}
		},
		C3 {
			/**
			 * ">c?<" -> "b+?"
			 */
			@Override
			public String optimize(String s) {
				return s.replaceAll(">c", "b+")
						.replaceAll(">C", "B+")
						.replace("<", "");
			}
		};

		public BmCpState nextStatus(String token) {
			return NONE;
		}

		public String optimize(String s) {
			throw new AssertionError();
		}
	}

	private StringBuilder builder = new StringBuilder();
	private StringBuilder optBuilder = new StringBuilder();
	private BmCpState state = BmCpState.NONE;

	@Override
	public void nextToken(String token) {
		BmCpState prevStatus = state;
		state = state.nextStatus(token);
		if (MMLStringOptimizer.getDebug()) {
			System.out.println(prevStatus + " - > " + state);
		}

		if (state == BmCpState.NONE) {
			builder.append(optBuilder);
			optBuilder.setLength(0);
			builder.append(token);
		} else {
			optBuilder.append(token);
			if  ( (state == BmCpState.B3) || (state == BmCpState.C3) )  {
				builder.append( state.optimize( optBuilder.toString() ));
				optBuilder.setLength(0);
				state = state.nextStatus(null);
			}
		}
	}

	@Override
	public String getMinString() {
		builder.append(optBuilder);
		optBuilder.setLength(0);
		return builder.toString();
	}
}
