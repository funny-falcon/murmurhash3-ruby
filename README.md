# Murmurhash3

This is an implementation of [MurmurHash3](http://code.google.com/p/smhasher/wiki/MurmurHash3) -
noncriptographic hash function.

It includes x86\_32bit variant and x64\_128bit variant. x86\_128bit variant is ommited.

It exposes finalization mix functions as variant of superfast integer hashing.

## Installation

Add this line to your application's Gemfile:

    gem 'murmurhash3'

And then execute:

    $ bundle

Or install it yourself as:

    $ gem install murmurhash3

## Usage

```ruby
    require 'murmurhash3'

    MurmurHash3::V32.str_digest(some_string, seed)
    MurmurHash3::V32.str_hexdigest(some_string, seed)
    MurmurHash3::V32.str_base64digest(some_string, seed)
    MurmurHash3::V32.fmix(some_32bit_integer)
    MurmurHash3::V32.str_hash(some_string)
    MurmurHash3::V32.str_hash(some_string, seed)
    MurmurHash3::V32.int32_hash(some_32bit_integer)
    MurmurHash3::V32.int32_hash(some_32bit_integer, seed)
    MurmurHash3::V32.int64_hash(some_64bit_integer)
    MurmurHash3::V32.int64_hash(some_64bit_integer, seed)

    class SomeClass
      include MurmurHash3::V32
      def func
        murmur3_32_str_digest(some_string)
        murmur3_32_str_hexdigest(some_string)
        murmur3_32_str_base64digest(some_string)
        murmur3_32_fmix(some_32bit_integer)
        murmur3_32_str_hash(some_string)
        murmur3_32_str_hash(some_string, seed)
        murmur3_32_int32_hash(some_32bit_integer)
        murmur3_32_int32_hash(some_32bit_integer, seed)
        murmur3_32_int64_hash(some_64bit_integer)
        murmur3_32_int64_hash(some_64bit_integer, seed)
      end
    end

    MurmurHash3::V128.str_digest(some_string, seed)        # => String
    MurmurHash3::V128.str_hexdigest(some_string, seed)     # => String
    MurmurHash3::V128.str_base64digest(some_string, seed)  # => String
    MurmurHash3::V128.fmix(some_64bit_integer)             # => int64
    MurmurHash3::V128.str_hash(some_string)                # => [int32_0, int32_1, int32_2, int32_3]
    MurmurHash3::V128.str_hash(some_string, seed)          # => [int32_0, int32_1, int32_2, int32_3]
    MurmurHash3::V128.int32_hash(some_32bit_integer)       # => [int32_0, int32_1, int32_2, int32_3]
    MurmurHash3::V128.int32_hash(some_32bit_integer, seed) # => [int32_0, int32_1, int32_2, int32_3]
    MurmurHash3::V128.int64_hash(some_64bit_integer)       # => [int32_0, int32_1, int32_2, int32_3]
    MurmurHash3::V128.int64_hash(some_64bit_integer, seed) # => [int32_0, int32_1, int32_2, int32_3]

    class SomeClass
      include MurmurHash3::V128
      def func
        murmur3_128_str_digest(some_string)              # => String
        murmur3_128_str_hexdigest(some_string)           # => String
        murmur3_128_str_base64digest(some_string)        # => String
        murmur3_128_fmix(some_64bit_integer)             # => int64
        murmur3_128_str_hash(some_string)                # => [int32_0, int32_1, int32_2, int32_3]
        murmur3_128_str_hash(some_string, seed)          # => [int32_0, int32_1, int32_2, int32_3]
        murmur3_128_int32_hash(some_32bit_integer)       # => [int32_0, int32_1, int32_2, int32_3]
        murmur3_128_int32_hash(some_32bit_integer, seed) # => [int32_0, int32_1, int32_2, int32_3]
        murmur3_128_int64_hash(some_64bit_integer)       # => [int32_0, int32_1, int32_2, int32_3]
        murmur3_128_int64_hash(some_64bit_integer, seed) # => [int32_0, int32_1, int32_2, int32_3]
      end
    end
```


## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request
