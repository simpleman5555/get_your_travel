package com.getyourtravel.entities.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @PersistenceContext private EntityManager entityManager;
    @Value("${pagination.product-elements-per-page}") private int defaultNumOfElementsPerPage;

    public Product findById(long id) {
        return productRepository.findById(id);
    }

    public List<Product> findByCategoryId(long categoryId) { return productRepository.findByCategoryId(categoryId); }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product findByNameAndCategoryIdAndIdNot(String productName, long categoryId, long id) {
        return productRepository.findByNameAndCategoryIdAndIdNot(productName, categoryId, id);
    }

    public void deleteProduct(long id) {
        productRepository.deleteById(id);
    }

    public Integer getLastSortingValue() {
        return productRepository.getLastSortingValue();
    }

    public Page<Product> findPage(int pageNumber, int numOfElementsPerPage, long categoryId) {
        Pageable pageable = PageRequest.of(pageNumber - 1, numOfElementsPerPage);
        int notNeededQuantity = (pageNumber - 1) * defaultNumOfElementsPerPage;
        int maxQuantity = notNeededQuantity + numOfElementsPerPage;
        List<Product> neededProductList = new ArrayList<>();
        if (categoryId > 0) {
            if (pageNumber > 1) {
                List<Product> productList = productRepository.findByCategoryIdAndSorting(categoryId, maxQuantity);
                for (int i = notNeededQuantity; i < productList.size(); i++) {
                    neededProductList.add(productList.get(i));
                }
                Page<Product> page = new PageImpl<>(neededProductList, pageable, neededProductList.size());
                return page;
            }
            return productRepository.findByCategoryIdAndSorting(categoryId, pageable);
        } else {
            if (pageNumber > 1) {
                List<Product> productList = productRepository.findAllBySorting(maxQuantity);
                for (int i = notNeededQuantity; i < productList.size(); i++) {
                    neededProductList.add(productList.get(i));
                }
                Page<Product> page = new PageImpl<>(neededProductList, pageable, neededProductList.size());
                return page;
            }
            return productRepository.findAllBySorting(pageable);
        }
    }

    public Page<Product> findPageWithSorting(String field, String sortDir, int pageNumber, int numOfElementsPerPage, long categoryId) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber - 1, numOfElementsPerPage, sort);
        if (categoryId > 0) {
            return productRepository.findByCategoryId(categoryId, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    // here we handle the sorting values of Products when we click and hold the mouse on a product and pull it up or down (reordering) - reordering works just with mouse,
    // so not on tablet or on mobile
    public void manageSortingValuesIfReorderPage(long[] ids, int pageNumber, String field, String sortDirection, int numOfElementsPerPage) {
        int count;
        int numOfElementsOfLastPage;
        int totalPages = findAll().size() / numOfElementsPerPage;

        if ((findAll().size() % numOfElementsPerPage) != 0) {
            totalPages++;
        }
        if ((findAll().size() % numOfElementsPerPage) == 0) {
            numOfElementsOfLastPage = numOfElementsPerPage;
        } else {
            numOfElementsOfLastPage = findAll().size() % numOfElementsPerPage;
        }

        // we add new sorting values to the products which are on the current page depends on the sorting direction (descending, ascending)
        if (sortDirection.equals("desc")) {
            count = (totalPages * numOfElementsPerPage) - ((pageNumber - 1) * numOfElementsPerPage) - (numOfElementsPerPage - numOfElementsOfLastPage);
            for (long id : ids) {
                Product product = findById(id);
                product.setSorting(count);
                save(product);
                count--;
            }
        } else {
            count = (numOfElementsPerPage * (pageNumber - 1)) + 1;
            for (long id : ids) {
                Product product = findById(id);
                product.setSorting(count);
                save(product);
                count++;
            }
        }

        // we add new sorting values to the products which are before the current page depends on the sorting direction (descending, ascending)
        // this step is necessary just if the sorting field isn't "sorting" but something else
        if (pageNumber > 1) {
            int originalPageNumber = pageNumber;
            if (! field.equals("sorting")) {
                pageNumber--;
                for (int i = pageNumber; i > 0; i--) {

                    count = sortDirection.equals("asc") ?
                            (count - (2 * numOfElementsPerPage)) : (count + (2 * numOfElementsPerPage));

                    if (originalPageNumber == totalPages) {
                        count = sortDirection.equals("asc") ?
                            (count + (numOfElementsPerPage - numOfElementsOfLastPage)) :
                            (count - (numOfElementsPerPage - numOfElementsOfLastPage));
                    }

                    Page<Product> page = findPageWithSorting(field, sortDirection, i, numOfElementsPerPage, 0);
                    List<Product> productList = page.getContent();
                    for (Product product : productList) {
                        product.setSorting(count);
                        save(product);
                        count = sortDirection.equals("asc") ? ++count : --count;
                    }
                }
                pageNumber = originalPageNumber;
            }
        }

        // we add new sorting values to the products which are after the current page depends on the sorting direction (descending, ascending)
        if (totalPages > pageNumber) {
            if (sortDirection.equals("desc")) {
                count = (totalPages * numOfElementsPerPage) - (pageNumber * numOfElementsPerPage) - (numOfElementsPerPage - numOfElementsOfLastPage);
            } else {
                count = (numOfElementsPerPage * pageNumber) + 1;
            }
            pageNumber++;
            for (int i = pageNumber; i < totalPages + 1; i++) {
                Page<Product> page = findPageWithSorting(field, sortDirection, i, numOfElementsPerPage, 0);
                List<Product> productList = page.getContent();
                for (Product product : productList) {
                    product.setSorting(count);
                    save(product);
                    count = sortDirection.equals("asc") ? ++count : --count;
                }
            }
        }
    }

    // we have to add new sorting values to the products if we delete a product and that's what we do here
    public void manageSortingValuesIfDeleteProduct(long id, String field, String sortDirection) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(Product.class);
        Root<Product> root = criteriaQuery.from(Product.class);
        criteriaQuery.select(root);
        criteriaQuery.orderBy(sortDirection.equals("asc") ? criteriaBuilder.asc(root.get(field)) : criteriaBuilder.desc(root.get(field)));

        List<Product> productList = entityManager.createQuery(criteriaQuery).getResultList();

        Product productForDeletion = findById(id);
        productList.remove(productForDeletion);

        int maxSortingValue = productList.size();
        int newSortingValue = sortDirection.equals("asc") ? 1 : maxSortingValue;

        for (int i = 0; i < productList.size(); i++) {
            Product product = productList.get(i);
            product.setSorting(newSortingValue);
            save(product);
            newSortingValue = sortDirection.equals("asc") ? ++newSortingValue : --newSortingValue;
        }
    }
}