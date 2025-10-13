const Products = () => {
  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Products</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {/* Products will be fetched and rendered here */}
        <div className="text-center text-gray-500 col-span-full">
          Products will be loaded from the backend API
        </div>
      </div>
    </div>
  )
}

export default Products