/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mmlTools.optimizer;

import fourthline.mmlTools.core.MMLTokenizer;


/**
 * "<b>" -> "c-"
 * ">c<" -> "b+"
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
			 * "<b" -> B2
			 */
			@Override
			public BmCpState nextStatus(String token) {
				if (MMLTokenizer.isNote(token.charAt(0))) {
					if (MMLTokenizer.noteNames(token)[0].toLowerCase().equals("b")) {
						return B2;
					}
					return NONE;
				} else if (token.equals(">")) {
					return C1;
				}
				return B1;
			}
		},
		B2 {
			/**
			 * "<b>" -> B3
			 */
			@Override
			public BmCpState nextStatus(String token) {
				return (token.equals(">")) ? B3 : NONE;
			}
		},
		B3 {
			/**
			 * "<b>" -> "c-"
			 */
			@Override
			public String optimize(StringBuilder sb) {
				sb.deleteCharAt( sb.lastIndexOf("<") );
				sb.deleteCharAt( sb.lastIndexOf(">") );
				String s = sb.toString()
						.replace("b", "c-")
						.replace("B", "C-");
				return s;
			}
		},
		C1 {
			/**
			 * ">c" -> C2
			 */
			@Override
			public BmCpState nextStatus(String token) {
				if (MMLTokenizer.isNote(token.charAt(0))) {
					if (MMLTokenizer.noteNames(token)[0].toLowerCase().equals("c")) {
						return C2;
					}
					return NONE;
				} else if (token.equals("<")) {
					return B1;
				}
				return C1;
			}
		},
		C2 {
			/**
			 * ">c<" -> C3
			 */
			@Override
			public BmCpState nextStatus(String token) {
				return (token.equals("<")) ? C3 : NONE;
			}
		},
		C3 {
			/**
			 * ">c<" -> "b+"
			 */
			@Override
			public String optimize(StringBuilder sb) {
				sb.deleteCharAt( sb.lastIndexOf("<") );
				sb.deleteCharAt( sb.lastIndexOf(">") );
				String s = sb.toString()
						.replace("c", "b+")
						.replace("C", "B+");
				return s;
			}
		};

		public BmCpState nextStatus(String token) {
			return NONE;
		}

		public String optimize(StringBuilder sb) {
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
			System.out.println(prevStatus + " - > " + state + ": " + token);
		}

		if (state == BmCpState.NONE) {
			builder.append(optBuilder);
			optBuilder.setLength(0);
			builder.append(token);
		} else {
			optBuilder.append(token);
			if  ( (state == BmCpState.B3) || (state == BmCpState.C3) )  {
				builder.append( state.optimize( optBuilder ));
				optBuilder.setLength(0);
				state = state.nextStatus(token);
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
