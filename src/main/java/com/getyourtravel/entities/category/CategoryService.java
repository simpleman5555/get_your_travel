package com.getyourtravel.entities.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Category findById(long id) {
        return categoryRepository.findById(id);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Category findByCategoryName(String name) {
        return categoryRepository.findByName(name);
    }

    public Category findByCategoryNameANdIdNot(String name, long id) {
        return categoryRepository.findByNameAndIdNot(name, id);
    }

    public void deleteCategory(long id) {
        categoryRepository.deleteById(id);
    }

    public Integer getLastSortingValue() {
        return categoryRepository.getLastSortingValue();
    }

    public Page<Category> findPageWithSorting(String field, String sortDirection, int pageNumber, int numOfElementsPerPage) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber - 1, numOfElementsPerPage, sort);
        return categoryRepository.findAll(pageable);
    }

    public List<Category> findAllBySorting() {
        return categoryRepository.findAllBySorting();
    }

    // here we handle the sorting values of Categories when we click and hold the mouse on a category and pull it up or down (reordering) - reordering works just with mouse,
    // so not on tablet or on mobile
    public void manageSortingValuesIfReorderPage(long[]ids, int pageNumber, String field, String sortDirection, int numOfElementsPerPage) {
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

        // we add new sorting values to the categories which are on the current page depends on the sorting direction (descending, ascending)
        if (sortDirection.equals("desc")) {
            count = (totalPages * numOfElementsPerPage) - ((pageNumber - 1) * numOfElementsPerPage) - (numOfElementsPerPage - numOfElementsOfLastPage);
            for (long id : ids) {
                Category category = findById(id);
                category.setSorting(count);
                save(category);
                count--;
            }
        } else {
            count = (numOfElementsPerPage * (pageNumber - 1)) + 1;
            for (long id : ids) {
                Category category = findById(id);
                category.setSorting(count);
                save(category);
                count++;
            }
        }

        // we add new sorting values to the categories which are before the current page depends on the sorting direction (descending, ascending)
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

                    Page<Category> page = findPageWithSorting(field, sortDirection, i, numOfElementsPerPage);
                    List<Category> categoryList = page.getContent();
                    for (Category category : categoryList) {
                        category.setSorting(count);
                        save(category);
                        count = sortDirection.equals("asc") ? ++count : --count;
                    }
                }
                pageNumber = originalPageNumber;
            }
        }

        // we add new sorting values to the categories which are after the current page depends on the sorting direction (descending, ascending)
        if (totalPages > pageNumber) {
            if (sortDirection.equals("desc")) {
                count = (totalPages * numOfElementsPerPage) - (pageNumber * numOfElementsPerPage) - (numOfElementsPerPage - numOfElementsOfLastPage);
            } else {
                count = (numOfElementsPerPage * pageNumber) + 1;
            }
            pageNumber++;
            for (int i = pageNumber; i < totalPages + 1; i++) {
                Page<Category> page = findPageWithSorting(field, sortDirection, i, numOfElementsPerPage);
                List<Category> categoryList = page.getContent();
                for (Category category : categoryList) {
                    category.setSorting(count);
                    save(category);
                    count = sortDirection.equals("asc") ? ++count : --count;
                }
            }
        }
    }

    // we have to add new sorting values to the categories if we delete a category and that's what we do here
    public void manageSortingValuesIfDeleteCategory(long id, String field, String sortDirection) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Category> criteriaQuery = criteriaBuilder.createQuery(Category.class);
        Root<Category> root = criteriaQuery.from(Category.class);
        criteriaQuery.select(root);
        criteriaQuery.orderBy(sortDirection.equals("asc") ? criteriaBuilder.asc(root.get(field)) : criteriaBuilder.desc(root.get(field)));

        List<Category> categoryList = entityManager.createQuery(criteriaQuery).getResultList();

        Category categoryForDeletion = findById(id);
        categoryList.remove(categoryForDeletion);

        int maxSortingValue = categoryList.size();
        int newSortingValue = sortDirection.equals("asc") ? 1 : maxSortingValue;

        for (int i = 0; i < categoryList.size(); i++) {
            Category category = categoryList.get(i);
            category.setSorting(newSortingValue);
            save(category);
            newSortingValue = sortDirection.equals("asc") ? ++newSortingValue : --newSortingValue;
        }
    }
}