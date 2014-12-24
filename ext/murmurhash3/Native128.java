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

@JRubyModule(name="MurmurHash3::Native128")
public class Native128 extends MurmurBase {
	static private final long _fmix(long k) {
		k ^= k >>> 33;
		k *= 0xff51afd7ed558ccdl;
		k ^= k >>> 33;
		k *= 0xc4ceb9fe1a85ec53l;
		k ^= k >>> 33;

		return k;
	}

	static private final long _c1 = 0x87c37b91114253d5l;
	static private final long _c2 = 0x4cf5ad432745937fl;

	static private final long _mmix1(long k) {
		k *= _c1;
		k = (k << 31) | (k >>> 33);
		return k * _c2;
	}

	static private final long _mmix2(long k) {
		k *= _c2;
		k = (k << 33) | (k >>> 31);
		return k * _c1;
	}

	static private final long[] _hash(byte[] data, int from, int len, long seed) {
		long h1 = seed;
		long h2 = seed;
		int i, b = len & ~15;
		for (i = 0; i < b; i+=16) {
			long k1 = read64(data, from+i);
			long k2 = read64(data, from+i+8);

			h1 ^= _mmix1(k1);
			h1 = (h1 << 27) | (h1 >>> 37);
			h1 += h2;
			h1 = h1*5 + 0x52dce729;

			h2 ^= _mmix2(k2);
			h2 = (h2 << 31) | (h2 >>> 33);
			h2 += h1;
			h2 = h2*5 + 0x38495ab5;
		}
		if ((len & 8) != 0) {
			long k1 = read64(data, from+i);
			h1 ^= _mmix1(k1);
			if ((len & 7) != 0) {
				long k2 = read56(data, from+i+8, len&7);
				h2 ^= _mmix2(k2);
			}
		} else if ((len & 7) != 0) {
			long k1 = read56(data, from+i, len&7);
			h1 ^= _mmix1(k1);
		}
		h1 ^= len; h2 ^= len;
		h1 += h2; h2 += h1;
		h1 = _fmix(h1); h2 = _fmix(h2);
		h1 += h2; h2 += h1;
		long[] res = {h1, h2};
		return res;
	}

	@JRubyMethod(name = "murmur3_128_fmix")
	public static IRubyObject fmix64(ThreadContext context, IRubyObject self, IRubyObject oint) {
		long i = num2ulong(oint);
		i = _fmix(i);
		return ulong2num(context, i);
	}

	@JRubyMethod(name = "murmur3_128_str_hash")
	public static IRubyObject hash(ThreadContext context, IRubyObject self, IRubyObject ostr)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		long[] h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), 0);
		IRubyObject[] r = {
			RubyFixnum.newFixnum(context.runtime, h[0] & 0xffffffffl),
			RubyFixnum.newFixnum(context.runtime, h[0] >>> 32),
			RubyFixnum.newFixnum(context.runtime, h[1] & 0xffffffffl),
			RubyFixnum.newFixnum(context.runtime, h[1] >>> 32)
		};
		return RubyArray.newArrayNoCopyLight(context.runtime, r);
	}

	static private final IRubyObject makeRes(ThreadContext context, long[] h) {
		IRubyObject[] r = {
			RubyFixnum.newFixnum(context.runtime, h[0] & 0xffffffffl),
			RubyFixnum.newFixnum(context.runtime, h[0] >>> 32),
			RubyFixnum.newFixnum(context.runtime, h[1] & 0xffffffffl),
			RubyFixnum.newFixnum(context.runtime, h[1] >>> 32)
		};
		return RubyArray.newArrayNoCopyLight(context.runtime, r);
	}

	@JRubyMethod(name = "murmur3_128_str_hash")
	public static IRubyObject hash(ThreadContext context, IRubyObject self, IRubyObject ostr, IRubyObject oseed)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		long seed = num2ulong(oseed);
		long[] h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), seed);
		return makeRes(context, h);
	}

	@JRubyMethod(name = "murmur3_128_str_digest")
	public static IRubyObject digest(ThreadContext context, IRubyObject self, IRubyObject ostr)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		long[] h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), 0);
		byte[] d = digest128(h[0], h[1]);
		return RubyString.newStringNoCopy(context.runtime, d);
	}
	
	@JRubyMethod(name = "murmur3_128_str_digest")
	public static IRubyObject digest(ThreadContext context, IRubyObject self, IRubyObject ostr, IRubyObject oseed)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		long s = num2ulong(oseed);
		long[] h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), s);
		byte[] d = digest128(h[0], h[1]);
		return RubyString.newStringNoCopy(context.runtime, d);
	}
	
	@JRubyMethod(name = "murmur3_128_str_hexdigest")
	public static IRubyObject hexdigest(ThreadContext context, IRubyObject self, IRubyObject ostr)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		long[] h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), 0);
		byte[] d = digest128(h[0], h[1]);
		return hexdigest(context, d);
	}
	
	@JRubyMethod(name = "murmur3_128_str_hexdigest")
	public static IRubyObject hexdigest(ThreadContext context, IRubyObject self, IRubyObject ostr, IRubyObject oseed)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		long s = num2ulong(oseed);
		long[] h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), s);
		byte[] d = digest128(h[0], h[1]);
		return hexdigest(context, d);
	}
	
	@JRubyMethod(name = "murmur3_128_str_base64digest")
	public static IRubyObject base64digest(ThreadContext context, IRubyObject self, IRubyObject ostr)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		long[] h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), 0);
		byte[] d = digest128(h[0], h[1]);
		return base64digest(context, d);
	}
	
	@JRubyMethod(name = "murmur3_128_str_base64digest")
	public static IRubyObject base64digest(ThreadContext context, IRubyObject self, IRubyObject ostr, IRubyObject oseed)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		long s = num2ulong(oseed);
		long[] h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), s);
		byte[] d = digest128(h[0], h[1]);
		return base64digest(context, d);
	}

	@JRubyMethod(name = "murmur3_128_int32_hash")
	public static IRubyObject int32_hash(ThreadContext context, IRubyObject self, IRubyObject oint)
	{
		int i = RubyNumeric.fix2int(oint);
		byte[] d = digest32(i);
		long[] h = _hash(d, 0, 4, 0);
		return makeRes(context, h);
	}

	@JRubyMethod(name = "murmur3_128_int32_hash")
	public static IRubyObject int32_hash(ThreadContext context, IRubyObject self, IRubyObject oint, IRubyObject oseed)
	{
		int i = (int)RubyNumeric.num2long(oint);
		long s = num2ulong(oseed);
		byte[] d = digest32(i);
		long[] h = _hash(d, 0, 4, s);
		return makeRes(context, h);
	}

	@JRubyMethod(name = "murmur3_128_int64_hash")
	public static IRubyObject int64_hash(ThreadContext context, IRubyObject self, IRubyObject oint)
	{
		long v = num2ulong(oint);
		byte[] d = digest64(v);
		long[] h = _hash(d, 0, 8, 0);
		return makeRes(context, h);
	}

	@JRubyMethod(name = "murmur3_128_int64_hash")
	public static IRubyObject int64_hash(ThreadContext context, IRubyObject self, IRubyObject oint, IRubyObject oseed)
	{
		long v = num2ulong(oint);
		long s = num2ulong(oseed);
		byte[] d = digest64(v);
		long[] h = _hash(d, 0, 8, s);
		return makeRes(context, h);
	}
}
