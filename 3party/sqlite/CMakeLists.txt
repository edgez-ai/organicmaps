add_library(sqlite3 STATIC sqlite3.c)

target_include_directories(sqlite3 PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")

# The SDK only reads trusted MBTiles archives and does not need SQLite's dynamic
# extension loader. Keep the embedded library small and consistent on every ABI.
target_compile_definitions(sqlite3 PRIVATE
  SQLITE_OMIT_DEPRECATED
  SQLITE_OMIT_LOAD_EXTENSION
  SQLITE_THREADSAFE=1
)

set_target_properties(sqlite3 PROPERTIES
  POSITION_INDEPENDENT_CODE ON
  UNITY_BUILD OFF
)
