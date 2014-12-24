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

@JRubyModule(name = "MurmurHash3::Native32")
public class Native32 extends MurmurBase {
	static final int _fmix(int h) {
		h ^= h >>> 16;
		h *= 0x85ebca6b;
		h ^= h >>> 13;
		h *= 0xc2b2ae35;
		h ^= h >>> 16;

		return h;
	}

	static final int _mmix(int k) {
		k *= 0xcc9e2d51;
		k = (k << 15) | (k >>> 17);
		return k * 0x1b873593;
	}

	static final int _hash(byte[] data, int from, int len, int seed) {
		int h = seed;
		int k;
		int i, b = len & ~3;
		for(i = 0; i < b; i+=4) {
			h ^= _mmix(read32(data, i+from));
			h = (h << 13) | (h >>> 19);
			h = h*5 + 0xe6546b64;
		}
		if (len % 4 != 0) {
			h ^= _mmix(read24(data, i + from, len & 3));
		}
		h ^= len;
		return _fmix(h);
	}

	@JRubyMethod(name = "murmur3_32_fmix")
	public static IRubyObject fmix32(ThreadContext context, IRubyObject self, IRubyObject oint)
	{
		int i = (int)RubyNumeric.num2long(oint);
		i = _fmix((int)i);
		return RubyFixnum.newFixnum(context.runtime, (long)i & 0xffffffffl);
	}

	@JRubyMethod(name = "murmur3_32_str_hash")
	public static IRubyObject hash(ThreadContext context, IRubyObject self, IRubyObject ostr)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		int h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), 0);
		return RubyFixnum.newFixnum(context.runtime, (long)h & 0xffffffffl);
	}

	@JRubyMethod(name = "murmur3_32_str_hash")
	public static IRubyObject hash(ThreadContext context, IRubyObject self, IRubyObject ostr, IRubyObject oseed)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		int s = (int)RubyNumeric.num2long(oseed);
		int h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), s);
		return RubyFixnum.newFixnum(context.runtime, (long)h & 0xffffffffl);
	}

	@JRubyMethod(name = "murmur3_32_str_digest")
	public static IRubyObject digest(ThreadContext context, IRubyObject self, IRubyObject ostr)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		int h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), 0);
		byte[] d = digest32(h);
		return RubyString.newStringNoCopy(context.runtime, d);
	}
	
	@JRubyMethod(name = "murmur3_32_str_digest")
	public static IRubyObject digest(ThreadContext context, IRubyObject self, IRubyObject ostr, IRubyObject oseed)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		int s = (int)RubyNumeric.num2long(oseed);
		int h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), s);
		byte[] d = digest32(h);
		return RubyString.newStringNoCopy(context.runtime, d);
	}
	
	@JRubyMethod(name = "murmur3_32_str_hexdigest")
	public static IRubyObject hexdigest(ThreadContext context, IRubyObject self, IRubyObject ostr)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		int h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), 0);
		byte[] d = digest32(h);
		return hexdigest(context, d);
	}
	
	@JRubyMethod(name = "murmur3_32_str_hexdigest")
	public static IRubyObject hexdigest(ThreadContext context, IRubyObject self, IRubyObject ostr, IRubyObject oseed)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		int s = (int)RubyNumeric.num2long(oseed);
		int h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), s);
		byte[] d = digest32(h);
		return hexdigest(context, d);
	}
	
	@JRubyMethod(name = "murmur3_32_str_base64digest")
	public static IRubyObject base64digest(ThreadContext context, IRubyObject self, IRubyObject ostr)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		int h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), 0);
		byte[] d = digest32(h);
		return base64digest(context, d);
	}
	
	@JRubyMethod(name = "murmur3_32_str_base64digest")
	public static IRubyObject base64digest(ThreadContext context, IRubyObject self, IRubyObject ostr, IRubyObject oseed)
	{
		RubyString str = ostr.asString();
		ByteList bl = str.getByteList();
		int s = (int)RubyNumeric.num2long(oseed);
		int h = _hash(bl.getUnsafeBytes(), bl.begin(), bl.getRealSize(), s);
		byte[] d = digest32(h);
		return base64digest(context, d);
	}

	@JRubyMethod(name = "murmur3_32_int32_hash")
	public static IRubyObject int32_hash(ThreadContext context, IRubyObject self, IRubyObject oint)
	{
		int h = (int)RubyNumeric.num2long(oint);
		byte[] d = digest32(h);
		int r = _hash(d, 0, 4, 0);
		return RubyFixnum.newFixnum(context.runtime, (long)r & 0xffffffffl);
	}

	@JRubyMethod(name = "murmur3_32_int32_hash")
	public static IRubyObject int32_hash(ThreadContext context, IRubyObject self, IRubyObject oint, IRubyObject oseed)
	{
		int h = (int)RubyNumeric.num2long(oint);
		int s = (int)RubyNumeric.num2long(oseed);
		byte[] d = digest32(h);
		int r = _hash(d, 0, 4, s);
		return RubyFixnum.newFixnum(context.runtime, (long)r & 0xffffffffl);
	}

	@JRubyMethod(name = "murmur3_32_int64_hash")
	public static IRubyObject int64_hash(ThreadContext context, IRubyObject self, IRubyObject oint)
	{
		long v = num2ulong(oint);
		byte[] d = digest64(v);
		int r = _hash(d, 0, 8, 0);
		return RubyFixnum.newFixnum(context.runtime, (long)r & 0xffffffffl);
	}

	@JRubyMethod(name = "murmur3_32_int64_hash")
	public static IRubyObject int64_hash(ThreadContext context, IRubyObject self, IRubyObject oint, IRubyObject oseed)
	{
		long v = num2ulong(oint);
		int s = (int)RubyNumeric.num2long(oseed);
		byte[] d = digest64(v);
		int r = _hash(d, 0, 8, s);
		return RubyFixnum.newFixnum(context.runtime, (long)r & 0xffffffffl);
	}
}
