if defined?(RUBY_ENGINE) && RUBY_ENGINE == 'ruby'
  require 'mkmf'
  create_makefile("murmurhash3/native")
else
  File.open(File.dirname(__FILE__) + "/Makefile", 'w') do |f|
    f.write("install:\n\t")
  end
end
