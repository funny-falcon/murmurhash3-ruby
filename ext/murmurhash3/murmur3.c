#include <ruby.h>
/*-----------------------------------------------------------------------------
 * MurmurHash3 was written by Austin Appleby, and is placed in the public
 * domain. The author hereby disclaims copyright to this source code.

 * Note - The x86 and x64 versions do _not_ produce the same results, as the
 * algorithms are optimized for their respective platforms. You can still
 * compile and run any of them on any platform, but your performance with the
 * non-native version will be less than optimal.
 */

typedef unsigned char uint8_t;
typedef unsigned int uint32_t;
#ifndef HAVE_STDINT_H
#if SIZEOF_LONG == 8
typedef unsigned long uint64_t;
#else
typedef unsigned long long uint64_t;
#endif
#endif

/*-----------------------------------------------------------------------------
 * Platform-specific functions and macros
 */

#ifdef __GNUC__
#define FORCE_INLINE __attribute__((always_inline))
#elif defined(_MSC_VER)
#define FORCE_INLINE  __forceinline
#else
#define FORCE_INLINE
#endif

#if defined(_MSC_VER)

#define ROTL32(x,y)  _rotl(x,y)
#define ROTL64(x,y)  _rotl64(x,y)

#define BIG_CONSTANT(x) (x)

#else

static inline FORCE_INLINE uint32_t
rotl32 ( uint32_t x, int8_t r )
{
  return (x << r) | (x >> (32 - r));
}

static inline FORCE_INLINE uint64_t
rotl64 ( uint64_t x, int8_t r )
{
  return (x << r) | (x >> (64 - r));
}

#define	ROTL32(x,y)	rotl32(x,y)
#define ROTL64(x,y)	rotl64(x,y)

#define BIG_CONSTANT(x) (x##LLU)
#endif

/* end platform specific */

/* Block read - if your platform needs to do endian-swapping or can only
 * handle aligned reads, do the conversion here */
#ifdef WORDS_BIGENDIAN
#define GCC_VERSION_SINCE(major, minor, patchlevel) \
   (defined(__GNUC__) && !defined(__INTEL_COMPILER) && \
    ((__GNUC__ > (major)) ||  \
     (__GNUC__ == (major) && __GNUC_MINOR__ > (minor)) || \
     (__GNUC__ == (major) && __GNUC_MINOR__ == (minor) && __GNUC_PATCHLEVEL__ >= (patchlevel))))
#if GCC_VERSION_SINCE(4,3,0)
# define swap32(x) __builtin_bswap32(x)
# define swap64(x) __builtin_bswap64(x)
#endif

#ifndef swap32
# define swap32(x)	((((x)&0xFF)<<24)	\
			|(((x)>>24)&0xFF)	\
			|(((x)&0x0000FF00)<<8)	\
			|(((x)&0x00FF0000)>>8)	)
#endif

#ifndef swap64
# ifdef HAVE_INT64_T
static inline FORCE_INLINE uint64_t
swap64(uint64_t x) {
    x = (x>>32) | (x << 32);
    x = ((x & BIG_CONSTANT(0xFFFF0000FFFF0000)) >> 16) |
        ((x & BIG_CONSTANT(0x0000FFFF0000FFFF)) << 16);
    return ((x & BIG_CONSTANT(0xFF00FF00FF00FF00)) >> 8) |
           ((x & BIG_CONSTANT(0x00FF00FF00FF00FF)) << 8);
}
# endif

#endif
static inline FORCE_INLINE uint32_t
getblock32(const uint32_t * p, int i)
{
    return swap32(p[i]);
}

static inline FORCE_INLINE uint64_t
getblock64(const uint64_t * p, int i)
{
    return swap64(p[i]);
}
#else
#define getblock32(p, i) (p[i])
#define getblock64(p, i) (p[i])
#endif

/* Finalization mix - force all bits of a hash block to avalanche */

static inline FORCE_INLINE uint32_t
fmix32 ( uint32_t h )
{
  h ^= h >> 16;
  h *= 0x85ebca6b;
  h ^= h >> 13;
  h *= 0xc2b2ae35;
  h ^= h >> 16;

  return h;
}

static inline FORCE_INLINE uint64_t
fmix64 ( uint64_t k )
{
  k ^= k >> 33;
  k *= BIG_CONSTANT(0xff51afd7ed558ccd);
  k ^= k >> 33;
  k *= BIG_CONSTANT(0xc4ceb9fe1a85ec53);
  k ^= k >> 33;

  return k;
}

static inline FORCE_INLINE uint32_t
mmix32(uint32_t k1)
{
    k1 *= 0xcc9e2d51;
    k1 = ROTL32(k1, 15);
    return k1 * 0x1b873593;
}

static uint32_t
MurmurHash3_x86_32 ( const void * key, long len, uint32_t seed)
{
  const uint8_t * data = (const uint8_t*)key;
  const int nblocks = (int)(len / 4);
  int i;

  uint32_t h1 = seed;
  uint32_t k1 = 0;


  /* body */

  const uint32_t * blocks = (const uint32_t *)(data + nblocks*4);

  for(i = -nblocks; i; i++)
  {
    h1 ^= mmix32(getblock32(blocks, i));
    h1 = ROTL32(h1,13); 
    h1 = h1*5+0xe6546b64;
  }

  /* tail */

  data += nblocks*4;

  switch(len & 3)
  {
  case 3: k1 ^= data[2] << 16;
  case 2: k1 ^= data[1] << 8;
  case 1: k1 ^= data[0];
          h1 ^= mmix32(k1);
  };

  /* finalization */

  h1 ^= len;

  h1 = fmix32(h1);

  return h1;
} 

#define C1_128 BIG_CONSTANT(0x87c37b91114253d5)
#define C2_128 BIG_CONSTANT(0x4cf5ad432745937f)

static inline FORCE_INLINE uint64_t
mmix128_1(uint64_t k1)
{
    k1 *= C1_128;
    k1 = ROTL64(k1, 31);
    return k1 * C2_128;
}

static inline FORCE_INLINE uint64_t
mmix128_2(uint64_t k2)
{
    k2 *= C2_128;
    k2 = ROTL64(k2, 33);
    return k2 * C1_128;
}

static void MurmurHash3_x64_128 ( const void * key, const long len,
                           const uint32_t seed, void * out )
{
  const uint8_t * data = (const uint8_t*)key;
  const int nblocks = (int)(len / 16);
  int i;

  uint64_t h1 = seed;
  uint64_t h2 = seed;
  uint64_t k1 = 0, k2 = 0;

  /* body */

  const uint64_t * blocks = (const uint64_t *)(data);

  for(i = 0; i < nblocks; i++)
  {
    k1 = getblock64(blocks, i*2+0);
    k2 = getblock64(blocks, i*2+1);

    h1 ^= mmix128_1(k1);
    h1 = ROTL64(h1,27); h1 += h2; h1 = h1*5+0x52dce729;

    h2 ^= mmix128_2(k2);
    h2 = ROTL64(h2,31); h2 += h1; h2 = h2*5+0x38495ab5;
  }

  /* tail */

  data += nblocks*16;
  k1 = k2 = 0;

  switch(len & 15)
  {
  case 15: k2 ^= (uint64_t)(data[14]) << 48;
  case 14: k2 ^= (uint64_t)(data[13]) << 40;
  case 13: k2 ^= (uint64_t)(data[12]) << 32;
  case 12: k2 ^= (uint64_t)(data[11]) << 24;
  case 11: k2 ^= (uint64_t)(data[10]) << 16;
  case 10: k2 ^= (uint64_t)(data[ 9]) << 8;
  case  9: k2 ^= (uint64_t)(data[ 8]) << 0;
           h2 ^= mmix128_2(k2);

  case  8: k1 ^= (uint64_t)(data[ 7]) << 56;
  case  7: k1 ^= (uint64_t)(data[ 6]) << 48;
  case  6: k1 ^= (uint64_t)(data[ 5]) << 40;
  case  5: k1 ^= (uint64_t)(data[ 4]) << 32;
  case  4: k1 ^= (uint64_t)(data[ 3]) << 24;
  case  3: k1 ^= (uint64_t)(data[ 2]) << 16;
  case  2: k1 ^= (uint64_t)(data[ 1]) << 8;
  case  1: k1 ^= (uint64_t)(data[ 0]) << 0;
           h1 ^= mmix128_1(k1);
  };

  /* finalization */

  h1 ^= len; h2 ^= len;

  h1 += h2;
  h2 += h1;

  h1 = fmix64(h1);
  h2 = fmix64(h2);

  h1 += h2;
  h2 += h1;

  ((uint64_t*)out)[0] = h1;
  ((uint64_t*)out)[1] = h2;
}

/* end of MurmurHash3 algorithm */

static VALUE
rb_fmix32(VALUE self, VALUE integer)
{
    uint32_t _int = NUM2UINT(integer);
    return UINT2NUM(fmix32(_int));
}

static VALUE
rb_fmix64(VALUE self, VALUE integer)
{
#if SIZEOF_LONG == 8
    uint64_t _int = NUM2ULONG(integer);
    return ULONG2NUM(fmix64(_int));
#else
    uint64_t _int = NUM2ULL(integer);
    return ULL2NUM(fmix64(_int));
#endif
}

static VALUE
rb_murmur3_32_str_hash(int argc, VALUE* argv, VALUE self)
{
    VALUE rstr;
    uint32_t result;

    if (argc == 0 || argc > 2) {
	rb_raise(rb_eArgError, "accept 1 or 2 arguments: (string[, seed])");
    }
    rstr = argv[0];
    StringValue(rstr);

    result = MurmurHash3_x86_32(RSTRING_PTR(rstr), RSTRING_LEN(rstr), argc == 1 ? 0 : NUM2UINT(argv[1]));

    return UINT2NUM(result);
}

static VALUE
rb_murmur3_32_int32_hash(int argc, VALUE* argv, VALUE self)
{
    VALUE rint;
    uint32_t _int;
    uint32_t result;

    if (argc == 0 || argc > 2) {
	rb_raise(rb_eArgError, "accept 1 or 2 arguments: (int32[, seed])");
    }
    _int = NUM2UINT(argv[0]);

    result = MurmurHash3_x86_32(&_int, 4, argc == 1 ? 0 : NUM2UINT(argv[1]));

    return UINT2NUM(result);
}

static VALUE
rb_murmur3_32_int64_hash(int argc, VALUE* argv, VALUE self)
{
    VALUE rint;
    uint64_t _int;
    uint32_t result;

    if (argc == 0 || argc > 2) {
	rb_raise(rb_eArgError, "accept 1 or 2 arguments: (int64[, seed])");
    }
#if SIZEOF_LONG == 8
    _int = NUM2ULONG(argv[0]);
#else
    _int = NUM2ULL(argv[0]);
#endif

    result = MurmurHash3_x86_32(&_int, 8, argc == 1 ? 0 : NUM2UINT(argv[1]));

    return UINT2NUM(result);
}

#define PREPARE_128_BIT()         \
    VALUE rstr, rseed, ar_result; \
    uint32_t result[4];           \

#define SWAP_128_BIT() do {    \
        uint32_t tmp;          \
        tmp = result[0];       \
        result[0] = result[1]; \
        result[1] = tmp;       \
        tmp = result[2];       \
        result[2] = result[3]; \
        result[3] = tmp;       \
} while (0)

#define RETURN_128_BIT()       \
    ar_result = rb_ary_new2(4);      \
    rb_ary_push(ar_result, UINT2NUM(result[0])); \
    rb_ary_push(ar_result, UINT2NUM(result[1])); \
    rb_ary_push(ar_result, UINT2NUM(result[2])); \
    rb_ary_push(ar_result, UINT2NUM(result[3])); \
    return ar_result

static VALUE
rb_murmur3_128_str_hash(int argc, VALUE* argv, VALUE self)
{
    VALUE rstr, ar_result;
    uint32_t result[4];

    if (argc == 0 || argc > 2) {
	rb_raise(rb_eArgError, "accept 1 or 2 arguments: (string[, seed])");
    }
    rstr = argv[0];
    StringValue(rstr);

    MurmurHash3_x64_128(RSTRING_PTR(rstr), RSTRING_LEN(rstr), argc == 1 ? 0 : NUM2UINT(argv[1]), result);
#if WORDS_BIGENDIAN
    SWAP_128_BIT();
#endif
    RETURN_128_BIT();
}

static VALUE
rb_murmur3_128_int32_hash(int argc, VALUE* argv, VALUE self)
{
    VALUE ar_result;
    uint32_t result[4], _int;

    if (argc == 0 || argc > 2) {
	rb_raise(rb_eArgError, "accept 1 or 2 arguments: (int32[, seed])");
    }
    _int = NUM2UINT(argv[0]);
    MurmurHash3_x64_128(&_int, 4, argc == 1 ? 0 : NUM2UINT(argv[1]), result);
#if WORDS_BIGENDIAN
    SWAP_128_BIT();
#endif
    RETURN_128_BIT();
}

static VALUE
rb_murmur3_128_int64_hash(int argc, VALUE* argv, VALUE self)
{
    VALUE ar_result;
    uint32_t result[4];
    uint64_t _int;

    if (argc == 0 || argc > 2) {
	rb_raise(rb_eArgError, "accept 1 or 2 arguments: (int64[, seed])");
    }
#if SIZEOF_LONG == 8
    _int = NUM2ULONG(argv[0]);
#else
    _int = NUM2ULL(argv[0]);
#endif
    MurmurHash3_x64_128(&_int, 8, argc == 1 ? 0 : NUM2UINT(argv[1]), result);
#if WORDS_BIGENDIAN
    SWAP_128_BIT();
#endif
    RETURN_128_BIT();
}

void
Init_native_murmur() {
    VALUE singleton;
    VALUE mod_murmur = rb_define_module("MurmurHash3");
    VALUE mod_murmur32 = rb_define_module_under(mod_murmur, "Native32");
    VALUE mod_murmur128 = rb_define_module_under(mod_murmur, "Native128");

    rb_define_method(mod_murmur32, "murmur3_32_fmix", rb_fmix32, 1);
    rb_define_method(mod_murmur32, "murmur3_32_str_hash", rb_murmur3_32_str_hash, -1);
    rb_define_method(mod_murmur32, "murmur3_32_int32_hash", rb_murmur3_32_int32_hash, -1);
    rb_define_method(mod_murmur32, "murmur3_32_int64_hash", rb_murmur3_32_int64_hash, -1);

    rb_extend_object(mod_murmur32, mod_murmur32);
    singleton = rb_singleton_class(mod_murmur32);
    rb_define_alias(singleton, "fmix", "murmur3_32_fmix");
    rb_define_alias(singleton, "str_hash", "murmur3_32_str_hash");
    rb_define_alias(singleton, "int32_hash", "murmur3_32_int32_hash");
    rb_define_alias(singleton, "int64_hash", "murmur3_32_int64_hash");


    rb_define_method(mod_murmur128, "murmur3_128_fmix", rb_fmix64, 1);
    rb_define_method(mod_murmur128, "murmur3_128_str_hash", rb_murmur3_128_str_hash, -1);
    rb_define_method(mod_murmur128, "murmur3_128_int32_hash", rb_murmur3_128_int32_hash, -1);
    rb_define_method(mod_murmur128, "murmur3_128_int64_hash", rb_murmur3_128_int64_hash, -1);

    rb_extend_object(mod_murmur128, mod_murmur128);
    singleton = rb_singleton_class(mod_murmur128);
    rb_define_alias(singleton, "fmix", "murmur3_128_fmix");
    rb_define_alias(singleton, "str_hash", "murmur3_128_str_hash");
    rb_define_alias(singleton, "int32_hash", "murmur3_128_int32_hash");
    rb_define_alias(singleton, "int64_hash", "murmur3_128_int64_hash");

}
