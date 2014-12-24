package murmurhash3;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyFixnum;
import org.jruby.RubyModule;
import org.jruby.RubyString;
import org.jruby.RubyNumeric;
import org.jruby.RubyBignum;
import org.jruby.util.ByteList;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.load.BasicLibraryService;
import org.jruby.anno.JRubyModule;
import org.jruby.anno.JRubyMethod;

public class NativeService implements BasicLibraryService {
	public boolean basicLoad(Ruby ruby) {
		RubyModule murmur = ruby.defineModule("MurmurHash3");
		RubyModule java32 = murmur.defineModuleUnder("Native32");
		RubyModule java128 = murmur.defineModuleUnder("Native128");
		java128.defineAnnotatedMethods(Native128.class);
		java32.defineAnnotatedMethods(Native32.class);
		return true;
	}
}

class MurmurBase {
	static protected final byte[] hex = {
		'0','0','0','1','0','2','0','3','0','4','0','5','0','6','0','7','0','8','0','9','0','a','0','b','0','c','0','d','0','e','0','f',
		'1','0','1','1','1','2','1','3','1','4','1','5','1','6','1','7','1','8','1','9','1','a','1','b','1','c','1','d','1','e','1','f',
		'2','0','2','1','2','2','2','3','2','4','2','5','2','6','2','7','2','8','2','9','2','a','2','b','2','c','2','d','2','e','2','f',
		'3','0','3','1','3','2','3','3','3','4','3','5','3','6','3','7','3','8','3','9','3','a','3','b','3','c','3','d','3','e','3','f',
		'4','0','4','1','4','2','4','3','4','4','4','5','4','6','4','7','4','8','4','9','4','a','4','b','4','c','4','d','4','e','4','f',
		'5','0','5','1','5','2','5','3','5','4','5','5','5','6','5','7','5','8','5','9','5','a','5','b','5','c','5','d','5','e','5','f',
		'6','0','6','1','6','2','6','3','6','4','6','5','6','6','6','7','6','8','6','9','6','a','6','b','6','c','6','d','6','e','6','f',
		'7','0','7','1','7','2','7','3','7','4','7','5','7','6','7','7','7','8','7','9','7','a','7','b','7','c','7','d','7','e','7','f',
		'8','0','8','1','8','2','8','3','8','4','8','5','8','6','8','7','8','8','8','9','8','a','8','b','8','c','8','d','8','e','8','f',
		'9','0','9','1','9','2','9','3','9','4','9','5','9','6','9','7','9','8','9','9','9','a','9','b','9','c','9','d','9','e','9','f',
		'a','0','a','1','a','2','a','3','a','4','a','5','a','6','a','7','a','8','a','9','a','a','a','b','a','c','a','d','a','e','a','f',
		'b','0','b','1','b','2','b','3','b','4','b','5','b','6','b','7','b','8','b','9','b','a','b','b','b','c','b','d','b','e','b','f',
		'c','0','c','1','c','2','c','3','c','4','c','5','c','6','c','7','c','8','c','9','c','a','c','b','c','c','c','d','c','e','c','f',
		'd','0','d','1','d','2','d','3','d','4','d','5','d','6','d','7','d','8','d','9','d','a','d','b','d','c','d','d','d','e','d','f',
		'e','0','e','1','e','2','e','3','e','4','e','5','e','6','e','7','e','8','e','9','e','a','e','b','e','c','e','d','e','e','e','f',
		'f','0','f','1','f','2','f','3','f','4','f','5','f','6','f','7','f','8','f','9','f','a','f','b','f','c','f','d','f','e','f','f'};
	static protected final byte[] base64 = {
		'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'};

	static protected IRubyObject hexdigest(ThreadContext context, byte[] bs) {
		byte[] out = new byte[bs.length*2];
		int i;
		for (i=0; i < bs.length; i++) {
			int p = 0xff & bs[i];
			out[i*2] = hex[p*2];
			out[i*2+1] = hex[p*2+1];
		}
		return RubyString.newStringNoCopy(context.runtime, out);
	}
	static protected IRubyObject base64digest(ThreadContext context, byte[] bs) {
		int limit = (bs.length + 2) / 3;
		byte[] out = new byte[limit * 4];
		int i;
		for (i=0; i < limit; i++) {
			int b64 = (0xff & bs[i*3]) << 16;
			if (i*3+1 < bs.length) {
				b64 |= (0xff & bs[i*3+1]) << 8;
			}
			if (i*3+2 < bs.length) {
				b64 |= (0xff & bs[i*3+2]) << 0;
			}
			out[i*4+0] = base64[(b64 >> 18) & 0x3f];
			out[i*4+1] = base64[(b64 >> 12) & 0x3f];
			out[i*4+2] = base64[(b64 >>  6) & 0x3f];
			out[i*4+3] = base64[(b64 >>  0) & 0x3f];
		}
		switch(bs.length % 3) {
			case 2:
				out[limit*4-1] = '=';
				break;
			case 1:
				out[limit*4-1] = '=';
				out[limit*4-2] = '=';
				break;
		}
		return RubyString.newStringNoCopy(context.runtime, out);
	}

	static protected final int read32(byte[] data, int pos) {
		return (0xff & data[pos]) |
			((0xff & data[pos+1]) << 8) |
			((0xff & data[pos+2]) << 16) |
			((0xff & data[pos+3]) << 24);
	}

	static protected final int read24(byte[] data, int pos, int len) {
		switch (len) {
			case 3:
				return (0xff & data[pos]) |
					((0xff & data[pos+1]) << 8) |
					((0xff & data[pos+2]) << 16);
			case 2:
				return (0xff & data[pos]) |
					((0xff & data[pos+1]) << 8);
			case 1:
				return (0xff & data[pos]);
		}
		return 0;
	}

	static protected final long read64(byte[] data, int pos) {
		return (long)(0xff & data[pos]) |
			((long)(0xff & data[pos+1]) << 8) |
			((long)(0xff & data[pos+2]) << 16) |
			((long)(0xff & data[pos+3]) << 24) |
			((long)(0xff & data[pos+4]) << 32) |
			((long)(0xff & data[pos+5]) << 40) |
			((long)(0xff & data[pos+6]) << 48) |
			((long)(0xff & data[pos+7]) << 56);
	}

	static protected final long read56(byte[] data, int pos, int len) {
		long res = 0;
		if ((len & 3) != 0) {
			res = read24(data, pos, len & 3);
		}
		if ((len & 4) != 0) {
			res |= (read32(data, pos + (len & 3)) & 0xffffffffl) << ((len & 3) * 8);
		}
		return res;
	}

	static protected final byte[] digest32(int h) {
		byte[] d = {
			(byte)h, (byte)(h >>> 8),
			(byte)(h >>> 16), (byte)(h >>> 24)
		};
		return d;
	}

	static protected final byte[] digest64(long h) {
		byte[] d = {
			(byte)h, (byte)(h >>> 8),
			(byte)(h >>> 16), (byte)(h >>> 24),
			(byte)(h >>> 32), (byte)(h >>> 40),
			(byte)(h >>> 48), (byte)(h >>> 56)
		};
		return d;
	}

	static protected final byte[] digest128(long h1, long h2) {
		byte[] d = {
			(byte)h1, (byte)(h1 >>> 8),
			(byte)(h1 >>> 16), (byte)(h1 >>> 24),
			(byte)(h1 >>> 32), (byte)(h1 >>> 40),
			(byte)(h1 >>> 48), (byte)(h1 >>> 56),
			(byte)h2, (byte)(h2 >>> 8),
			(byte)(h2 >>> 16), (byte)(h2 >>> 24),
			(byte)(h2 >>> 32), (byte)(h2 >>> 40),
			(byte)(h2 >>> 48), (byte)(h2 >>> 56)
		};
		return d;
	}

	static protected final long num2ulong(IRubyObject oint) {
		long v = (oint instanceof RubyBignum) ?
			RubyBignum.big2ulong((RubyBignum)oint) :
			RubyNumeric.num2long(oint);
		return v;
	}

	static protected final IRubyObject ulong2num(ThreadContext context, long v) {
		if (v < 0) {
			RubyBignum big = RubyBignum.newBignum(context.runtime, v >>> 32);
			big = (RubyBignum)big.op_lshift(RubyFixnum.newFixnum(context.runtime, 32));
			return big.op_plus(context, v & 0xffffffffl);
		} else {
			return RubyFixnum.newFixnum(context.runtime, v);
		}
	}
}

