/*
 * Copyright (C) 2015 たんらる
 */

package fourthline.mmlTools.optimizer;

import fourthline.mmlTools.core.MMLTokenizer;


/**
 * {@code "<c> -> b+"} と {@code ">b< -> c-"} を使った最適化を行います.
 */
public final class BpCmOptimizer implements MMLStringOptimizer.Optimizer {

	private enum BpCmState {
		NONE {
			/**
			 * "<" -> B1
			 * ">" -> C1
			 */
			@Override
			public BpCmState nextStatus(String token) {
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
			public BpCmState nextStatus(String token) {
				if (isNoteWithoutR(token)) {
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
			public BpCmState nextStatus(String token) {
				if (isNoteWithoutR(token)) {
					return NONE;
				} else if (token.equals(">")) {
					return B3;
				}
				return B2;
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
			public BpCmState nextStatus(String token) {
				if (isNoteWithoutR(token)) {
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
			public BpCmState nextStatus(String token) {
				if (isNoteWithoutR(token)) {
					return NONE;
				} else if (token.equals("<")) {
					return C3;
				}
				return C2;
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

		public BpCmState nextStatus(String token) {
			return NONE;
		}

		public String optimize(StringBuilder sb) {
			throw new AssertionError();
		}

		private static boolean isNoteWithoutR(String token) {
			char firstC = Character.toLowerCase( token.charAt(0) );
			if ("abcdefg".indexOf(firstC) >= 0) {
				return true;
			}
			return false;
		}
	}

	private StringBuilder builder = new StringBuilder();
	private StringBuilder optBuilder = new StringBuilder();
	private BpCmState state = BpCmState.NONE;

	@Override
	public void nextToken(String token) {
		BpCmState prevStatus = state;
		state = state.nextStatus(token);
		if (MMLStringOptimizer.getDebug()) {
			System.out.println(prevStatus + " - > " + state + ": " + token);
		}

		if (state == BpCmState.NONE) {
			builder.append(optBuilder);
			optBuilder.setLength(0);
			builder.append(token);
		} else {
			optBuilder.append(token);
			if  ( (state == BpCmState.B3) || (state == BpCmState.C3) )  {
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
